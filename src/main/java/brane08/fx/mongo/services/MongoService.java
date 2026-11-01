package brane08.fx.mongo.services;

import brane08.fx.mongo.config.Connection;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.stream.StreamSupport;

public class MongoService {

    static final System.Logger LOGGER = System.getLogger(MongoService.class.getName());

    public String execute() {
        throw new UnsupportedOperationException();
    }

    public boolean testConnection(Connection connection) {
        return testConnection(connection.servers(), connection.username(), connection.password(), connection.database());
    }

    public boolean testConnection(String servers, String username, String password, String database) {
        try (var client = client(servers, username, password, database)) {
            var names = client.getDatabase(database).listCollectionNames();
            LOGGER.log(System.Logger.Level.INFO, "Collections: {0}", StreamSupport.stream(names.spliterator(), false).count());
            return true;
        } catch (MongoException ex) {
            LOGGER.log(System.Logger.Level.ERROR, ex.getMessage(), ex);
            return false;
        }
    }

    private MongoClient client(String servers, String username, String password, String database) {
        return MongoClients.create(
                "mongodb+srv://%s:%s@%s/%s?authSource=admin".formatted(username, password, servers, database));
    }
}
