package brane08.fx.mongo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import brane08.fx.mongo.ApplicationException;
import brane08.fx.mongo.config.Connection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigServiceTest {

    @Test
    void saveAndLoad_roundTripsDecryptedPassword(@TempDir Path dir) throws IOException {
        var service = new ConfigService(dir);

        service.save("prod", "cluster.example.mongodb.net", "appuser", "s3cret", "admin");

        assertEquals(
                new Connection("prod", "cluster.example.mongodb.net", "appuser", "s3cret", "admin"),
                service.getConnectionData("prod"));
        assertEquals(List.of("prod"), service.getConnectionNames());
        assertTrue(Files.readString(dir.resolve("connections.conf")).contains("ENC:"));
    }

    @Test
    void save_sameName_overwritesEntry(@TempDir Path dir) {
        var service = new ConfigService(dir);
        service.save("prod", "old.example.net", "olduser", "oldpass", "olddb");
        service.save("staging", "stg.example.net", "stguser", "stgpass", "stgdb");

        service.save("prod", "new.example.net", "newuser", "newpass", "newdb");

        assertEquals(
                new Connection("prod", "new.example.net", "newuser", "newpass", "newdb"),
                service.getConnectionData("prod"));
        assertEquals(
                new Connection("staging", "stg.example.net", "stguser", "stgpass", "stgdb"),
                service.getConnectionData("staging"));
        assertEquals(List.of("prod", "staging"), service.getConnectionNames());
    }

    @Test
    void getConnectionData_unknownName_throwsApplicationException(@TempDir Path dir) {
        var service = new ConfigService(dir);

        assertThrows(ApplicationException.class, () -> service.getConnectionData("missing"));
    }

    @Test
    void load_plaintextPassword_returnedAsIs_andResaveEncrypts(@TempDir Path dir) throws IOException {
        Files.writeString(
                dir.resolve("connections.conf"),
                """
                connections = [
                  {
                    name = "legacy"
                    servers = "localhost"
                    username = "root"
                    password = "plaintext-secret"
                    database = "admin"
                  }
                ]
                """);

        var service = new ConfigService(dir);
        var loaded = service.getConnectionData("legacy");

        assertEquals("plaintext-secret", loaded.password());

        service.save(loaded.name(), loaded.servers(), loaded.username(), loaded.password(), loaded.database());

        assertTrue(Files.readString(dir.resolve("connections.conf")).contains("ENC:"));
        assertEquals("plaintext-secret", new ConfigService(dir).getConnectionData("legacy").password());
    }

    @Test
    void load_encPasswordWithoutKey_throwsApplicationException(@TempDir Path dir) throws IOException {
        var service = new ConfigService(dir);
        service.save("prod", "cluster.example.mongodb.net", "appuser", "pw", "admin");
        Files.delete(dir.resolve(".key"));

        assertThrows(ApplicationException.class, () -> service.getConnectionData("prod"));
    }

    @Test
    void undecryptableEntry_doesNotBreakListingOrSavingOthers(@TempDir Path dir) throws IOException {
        var service = new ConfigService(dir);
        service.save("orphan", "cluster.example.mongodb.net", "appuser", "pw", "admin");
        Files.delete(dir.resolve(".key"));

        assertEquals(List.of("orphan"), service.getConnectionNames());

        service.save("fresh", "f.example.net", "u", "p", "db");

        assertEquals(List.of("orphan", "fresh"), service.getConnectionNames());
        assertEquals("p", service.getConnectionData("fresh").password());
        assertThrows(ApplicationException.class, () -> service.getConnectionData("orphan"));
    }

    @Test
    void plaintextPasswordStartingWithEncPrefix_isReturnedAsIs(@TempDir Path dir) throws IOException {
        Files.writeString(
                dir.resolve("connections.conf"),
                """
                connections = [
                  { name = "odd", servers = "h", username = "u", password = "ENC:not-base64!", database = "db" }
                ]
                """);

        assertEquals("ENC:not-base64!", new ConfigService(dir).getConnectionData("odd").password());
    }

    @Test
    void malformedConfFile_throwsApplicationException(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("connections.conf"), "connections = [ { name = \n");
        var service = new ConfigService(dir);

        assertThrows(ApplicationException.class, service::getConnectionNames);
    }

    @Test
    void entryMissingField_throwsApplicationException(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("connections.conf"), "connections = [ { name = \"x\" } ]\n");
        var service = new ConfigService(dir);

        assertThrows(ApplicationException.class, service::getConnectionNames);
    }

    @Test
    void save_createsOwnerOnlyConfigDirectory(@TempDir Path parent) throws IOException {
        var dir = parent.resolve("cfg");
        new ConfigService(dir).save("a", "h", "u", "p", "db");

        if (dir.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            assertEquals(
                    java.nio.file.attribute.PosixFilePermissions.fromString("rwx------"),
                    Files.getPosixFilePermissions(dir));
        }
    }

    @Test
    void concurrentSaves_fromSeparateServices_loseNoUpdates(@TempDir Path dir) throws Exception {
        var threads = 8;
        var pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        try {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (var i = 0; i < threads; i++) {
                var name = "conn" + i;
                futures.add(pool.submit(() -> new ConfigService(dir).save(name, "h", "u", "p", "db")));
            }
            for (var future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(threads, new ConfigService(dir).getConnectionNames().size());
    }

    @Test
    void getConnectionNames_emptyWhenNoConfig(@TempDir Path dir) {
        assertEquals(List.of(), new ConfigService(dir).getConnectionNames());
    }

    @Test
    void getConnectionNames_preservesSaveOrder(@TempDir Path dir) {
        var service = new ConfigService(dir);
        service.save("alpha", "a.example.net", "u", "p", "db");
        service.save("beta", "b.example.net", "u", "p", "db");
        service.save("gamma", "c.example.net", "u", "p", "db");

        assertEquals(List.of("alpha", "beta", "gamma"), service.getConnectionNames());
    }

    @Test
    void loadConnections_emptyWhenConnectionsKeyMissing(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("connections.conf"), "other = 1\n");
        assertEquals(List.of(), new ConfigService(dir).getConnectionNames());
    }

    @Test
    void defaultConstructor_usesUserHomeDirectory(@TempDir Path home) {
        var previous = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        try {
            var service = new ConfigService();
            service.save("home", "h.example.net", "u", "p", "db");
            assertEquals(List.of("home"), service.getConnectionNames());
            assertTrue(Files.exists(home.resolve(".mongo-client-fx").resolve("connections.conf")));
        } finally {
            if (previous == null) {
                System.clearProperty("user.home");
            } else {
                System.setProperty("user.home", previous);
            }
        }
    }
}
