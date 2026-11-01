package brane08.fx.mongo.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import brane08.fx.mongo.ApplicationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecretBoxTest {

    @Test
    void roundTrip_encryptDecrypt_returnsOriginal(@TempDir Path dir) {
        var box = new SecretBox(dir.resolve("secret.key"));

        var encoded = box.encrypt("s3cr€t pässword");

        assertTrue(encoded.startsWith("ENC:"));
        assertEquals("s3cr€t pässword", box.decrypt(encoded));
    }

    @Test
    void roundTrip_secondInstanceWithSameKeyFile_decrypts(@TempDir Path dir) {
        var keyFile = dir.resolve("secret.key");
        var encoded = new SecretBox(keyFile).encrypt("hunter2");

        assertEquals("hunter2", new SecretBox(keyFile).decrypt(encoded));
    }

    @Test
    void encrypt_samePlaintextTwice_producesDifferentCiphertext(@TempDir Path dir) {
        var box = new SecretBox(dir.resolve("secret.key"));

        assertNotEquals(box.encrypt("same"), box.encrypt("same"));
    }

    @Test
    void encrypt_wireFormat_isIvThenCiphertextThenTag(@TempDir Path dir) {
        var box = new SecretBox(dir.resolve("secret.key"));

        var raw = Base64.getDecoder().decode(box.encrypt("abc").substring("ENC:".length()));

        // 12-byte IV + 3 bytes ciphertext + 16-byte GCM tag
        assertEquals(12 + 3 + 16, raw.length);
    }

    @Test
    void decrypt_missingKeyFile_throwsApplicationException(@TempDir Path dir) {
        var encoded = new SecretBox(dir.resolve("other.key")).encrypt("x");
        var box = new SecretBox(dir.resolve("absent.key"));

        assertThrows(ApplicationException.class, () -> box.decrypt(encoded));
    }

    @Test
    void decrypt_invalidKeyFile_throwsApplicationException(@TempDir Path dir) throws IOException {
        var encoded = new SecretBox(dir.resolve("good.key")).encrypt("x");
        var badKey = dir.resolve("bad.key");
        Files.write(badKey, new byte[5]);

        assertThrows(ApplicationException.class, () -> new SecretBox(badKey).decrypt(encoded));
    }

    @Test
    void decrypt_withoutEncPrefix_throwsApplicationException(@TempDir Path dir) {
        var box = new SecretBox(dir.resolve("secret.key"));
        box.encrypt("seed");

        assertThrows(ApplicationException.class, () -> box.decrypt("plain"));
    }

    @Test
    void decrypt_tamperedCiphertext_throwsApplicationException(@TempDir Path dir) {
        var box = new SecretBox(dir.resolve("secret.key"));
        var raw = Base64.getDecoder().decode(box.encrypt("secret").substring("ENC:".length()));
        raw[raw.length - 1] ^= 0x01;
        var tampered = "ENC:" + Base64.getEncoder().encodeToString(raw);

        assertThrows(ApplicationException.class, () -> box.decrypt(tampered));
    }

    @Test
    void decrypt_invalidBase64_throwsApplicationException(@TempDir Path dir) {
        var box = new SecretBox(dir.resolve("secret.key"));
        box.encrypt("seed");

        assertThrows(ApplicationException.class, () -> box.decrypt("ENC:!!!not-base64!!!"));
    }

    @Test
    void encrypt_concurrentFirstUse_allInstancesShareOneCompleteKey(@TempDir Path dir) throws Exception {
        var keyFile = dir.resolve("secret.key");
        int workers = 16;
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(workers);
        try {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < workers; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return new SecretBox(keyFile).encrypt("shared");
                }));
            }
            start.countDown();
            var encoded = new ArrayList<String>();
            for (var result : results) {
                encoded.add(result.get(30, TimeUnit.SECONDS));
            }

            assertEquals(32, Files.size(keyFile));
            var reader = new SecretBox(keyFile);
            for (var value : encoded) {
                assertEquals("shared", reader.decrypt(value));
            }
            try (var files = Files.list(dir)) {
                assertEquals(List.of(keyFile.getFileName().toString()), files.map(p -> p.getFileName().toString()).toList());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void encrypt_keyFileAlreadyPublished_isNotOverwritten(@TempDir Path dir) throws IOException {
        var keyFile = dir.resolve("secret.key");
        var existing = new SecretBox(keyFile);
        var encoded = existing.encrypt("first");
        var before = Files.readAllBytes(keyFile);

        new SecretBox(keyFile).encrypt("second");

        assertArrayEquals(before, Files.readAllBytes(keyFile));
        assertEquals("first", new SecretBox(keyFile).decrypt(encoded));
    }

    @Test
    void encrypt_createsKeyFileWithOwnerOnlyPermissionsWhenPosix(@TempDir Path dir) throws IOException {
        var keyFile = dir.resolve("nested").resolve("secret.key");

        new SecretBox(keyFile).encrypt("x");

        assertTrue(Files.exists(keyFile));
        assertEquals(32, Files.size(keyFile));
        if (keyFile.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(keyFile));
        }
    }
}
