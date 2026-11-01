package brane08.fx.mongo.services;

import brane08.fx.mongo.ApplicationException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-GCM encryption of short secrets such as passwords.
 *
 * <p>Wire form: {@code ENC:} + Base64(IV(12) || ciphertext || tag(16)). The 256-bit key is stored in a key file that is
 * created (owner-only permissions where POSIX is supported) on the first {@link #encrypt(String)} call.
 */
public final class SecretBox {

    public static final String PREFIX = "ENC:";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final Path keyFile;
    private final SecureRandom random = new SecureRandom();

    public SecretBox(Path keyFile) {
        this.keyFile = keyFile;
    }

    public String encrypt(String plaintext) {
        var key = loadOrCreateKey();
        var iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            var encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            var out = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(out.array());
        } catch (GeneralSecurityException e) {
            throw new ApplicationException("Unable to encrypt secret", e);
        }
    }

    public String decrypt(String encoded) {
        if (encoded == null || !encoded.startsWith(PREFIX)) {
            throw new ApplicationException("Value is not an encrypted secret (missing " + PREFIX + " prefix)");
        }
        var key = loadKey();
        try {
            var raw = Base64.getDecoder().decode(encoded.substring(PREFIX.length()));
            if (raw.length < IV_BYTES + TAG_BITS / 8) {
                throw new ApplicationException("Encrypted secret is too short");
            }
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, raw, 0, IV_BYTES));
            var plain = cipher.doFinal(raw, IV_BYTES, raw.length - IV_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new ApplicationException("Unable to decrypt secret", e);
        }
    }

    private synchronized SecretKeySpec loadOrCreateKey() {
        if (!Files.exists(keyFile)) {
            createKeyFile();
        }
        return loadKey();
    }

    /**
     * Writes the key to a same-directory temp file (owner-only), then publishes it without ever exposing a partial
     * file at the final path. If another process/instance publishes first, its key wins and is loaded afterwards.
     */
    private void createKeyFile() {
        var bytes = new byte[KEY_BYTES];
        random.nextBytes(bytes);
        Path temp = null;
        try {
            var dir = keyFile.toAbsolutePath().getParent();
            Files.createDirectories(dir);
            temp = Files.createTempFile(dir, keyFile.getFileName() + ".", ".tmp", ownerOnly());
            Files.write(temp, bytes);
            publish(temp);
        } catch (FileAlreadyExistsException e) {
            // another writer published first; use its key
        } catch (IOException e) {
            throw new ApplicationException("Unable to create key file " + keyFile, e);
        } finally {
            deleteQuietly(temp);
        }
    }

    /** Atomically makes {@code temp} visible as the key file, failing with {@link FileAlreadyExistsException} if present. */
    private void publish(Path temp) throws IOException {
        try {
            // A hard link is atomic and, unlike rename, never replaces an existing destination.
            Files.createLink(keyFile, temp);
        } catch (UnsupportedOperationException | SecurityException e) {
            moveWithoutReplacing(temp);
        } catch (FileAlreadyExistsException e) {
            throw e;
        } catch (IOException e) {
            moveWithoutReplacing(temp);
        }
    }

    /** Fallback for file systems without hard links; atomic rename may replace, so check for a winner first. */
    private void moveWithoutReplacing(Path temp) throws IOException {
        if (Files.exists(keyFile)) {
            throw new FileAlreadyExistsException(keyFile.toString());
        }
        try {
            Files.move(temp, keyFile, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, keyFile);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // best effort cleanup of the temp file
            }
        }
    }

    private FileAttribute<?>[] ownerOnly() {
        if (keyFile.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            return new FileAttribute<?>[] {PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))};
        }
        return new FileAttribute<?>[0];
    }

    private SecretKeySpec loadKey() {
        try {
            var bytes = Files.readAllBytes(keyFile);
            if (bytes.length != KEY_BYTES) {
                throw new ApplicationException(
                        "Invalid key file " + keyFile + ": expected " + KEY_BYTES + " bytes, found " + bytes.length);
            }
            return new SecretKeySpec(bytes, "AES");
        } catch (NoSuchFileException e) {
            throw new ApplicationException("Key file not found: " + keyFile, e);
        } catch (IOException e) {
            throw new ApplicationException("Unable to read key file " + keyFile, e);
        }
    }
}
