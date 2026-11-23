package brane08.fx.mongo.services;

import brane08.fx.mongo.config.Connection;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

public class MongoService {

    static final System.Logger LOGGER = System.getLogger(MongoService.class.getName());

    static final String SCHEME = "mongodb://";
    static final String SRV_SCHEME = "mongodb+srv://";
    static final String AUTH_SOURCE = "admin";

    public boolean testConnection(Connection connection) {
        return testConnection(connection.servers(), connection.username(), connection.password(), connection.database());
    }

    public boolean testConnection(String servers, String username, String password, String database) {
        try (var client = client(servers, username, password, database)) {
            var names = client.getDatabase(database).listCollectionNames();
            LOGGER.log(System.Logger.Level.INFO, "Collections: {0}", StreamSupport.stream(names.spliterator(), false).count());
            return true;
        } catch (MongoException | IllegalArgumentException ex) {
            LOGGER.log(System.Logger.Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }

    MongoClient client(String servers, String username, String password, String database) {
        var settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(uri(servers, database)));
        if (username != null && !username.isBlank()) {
            var secret = password == null ? new char[0] : password.toCharArray();
            settings.credential(MongoCredential.createCredential(username, AUTH_SOURCE, secret));
        }
        return MongoClients.create(settings.build());
    }

    static String uri(String servers, String database) {
        var hosts = servers.strip();
        if (!hosts.startsWith(SCHEME) && !hosts.startsWith(SRV_SCHEME)) {
            var plain = hosts.contains(":") || hosts.contains(",");
            hosts = (plain ? SCHEME : SRV_SCHEME) + hosts;
        }
        if (hosts.endsWith("/")) {
            hosts = hosts.substring(0, hosts.length() - 1);
        }
        return hosts + "/" + URLEncoder.encode(database, StandardCharsets.UTF_8) + "?authSource=" + AUTH_SOURCE;
    }
}
