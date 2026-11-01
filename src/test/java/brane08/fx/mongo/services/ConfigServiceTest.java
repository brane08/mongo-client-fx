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
        Files.writeString(
                dir.resolve("connections.conf"),
                """
                connections = [
                  {
                    name = "prod"
                    servers = "cluster.example.mongodb.net"
                    username = "appuser"
                    password = "ENC:dGFtpered=="
                    database = "admin"
                  }
                ]
                """);

        var service = new ConfigService(dir);

        assertThrows(ApplicationException.class, () -> service.getConnectionData("prod"));
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
