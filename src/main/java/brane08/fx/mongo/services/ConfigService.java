package brane08.fx.mongo.services;

import brane08.fx.mongo.ApplicationException;
import brane08.fx.mongo.config.Connection;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValueFactory;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ConfigService {

    static final String CONF_FILE = "connections.conf";
    static final String KEY_FILE = ".key";
    static final String CONNECTIONS = "connections";
    static final String DEFAULT_DIR = ".mongo-client-fx";
    static final String NAME = "name";
    static final String SERVERS = "servers";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String DATABASE = "database";

    Path configDir;
    Path confFile;
    SecretBox secretBox;

    public ConfigService() {
        this(Path.of(System.getProperty("user.home"), DEFAULT_DIR));
    }

    public ConfigService(Path configDir) {
        this.configDir = configDir;
        this.confFile = configDir.resolve(CONF_FILE);
        this.secretBox = new SecretBox(configDir.resolve(KEY_FILE));
    }

    public List<String> getConnectionNames() {
        return loadConnections().stream().map(Connection::name).toList();
    }

    public Connection getConnectionData(String name) {
        return loadConnections().stream()
                .filter(connection -> connection.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new ApplicationException("Unknown connection: " + name));
    }

    public void save(String name, String servers, String username, String password, String database) {
        var connections = new ArrayList<>(loadConnections());
        var updated = new Connection(name, servers, username, password, database);
        var existing = indexOf(connections, name);
        if (existing >= 0) {
            connections.set(existing, updated);
        } else {
            connections.add(updated);
        }
        writeConnections(connections);
    }

    List<Connection> loadConnections() {
        if (!Files.exists(confFile)) {
            return List.of();
        }
        var config = ConfigFactory.parseFile(confFile.toFile());
        if (!config.hasPath(CONNECTIONS)) {
            return List.of();
        }
        return config.getConfigList(CONNECTIONS).stream().map(this::toConnection).toList();
    }

    Connection toConnection(Config entry) {
        return new Connection(
                entry.getString(NAME),
                entry.getString(SERVERS),
                entry.getString(USERNAME),
                decryptPassword(entry.getString(PASSWORD)),
                entry.getString(DATABASE));
    }

    String decryptPassword(String password) {
        if (password.startsWith(SecretBox.PREFIX)) {
            return secretBox.decrypt(password);
        }
        return password;
    }

    void writeConnections(List<Connection> connections) {
        try {
            Files.createDirectories(configDir);
            var entries = connections.stream().map(this::toMap).toList();
            var rendered = ConfigFactory.empty()
                    .withValue(CONNECTIONS, ConfigValueFactory.fromIterable(entries))
                    .root()
                    .render(ConfigRenderOptions.defaults()
                            .setOriginComments(false)
                            .setComments(false)
                            .setJson(false));
            var temp = Files.createTempFile(configDir, "connections.", ".tmp");
            try {
                Files.writeString(temp, rendered);
                publish(temp);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new ApplicationException("Unable to save connections", e);
        }
    }

    void publish(Path temp) throws IOException {
        try {
            Files.move(temp, confFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException _) {
            Files.move(temp, confFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    Map<String, Object> toMap(Connection connection) {
        var map = new LinkedHashMap<String, Object>();
        map.put(NAME, connection.name());
        map.put(SERVERS, connection.servers());
        map.put(USERNAME, connection.username());
        map.put(PASSWORD, secretBox.encrypt(connection.password()));
        map.put(DATABASE, connection.database());
        return map;
    }

    int indexOf(List<Connection> connections, String name) {
        return IntStream.range(0, connections.size())
                .filter(i -> connections.get(i).name().equals(name))
                .findFirst()
                .orElse(-1);
    }
}
