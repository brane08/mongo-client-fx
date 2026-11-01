package brane08.fx.mongo.viewmodels;

import brane08.fx.mongo.config.Connection;
import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.services.MongoService;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConnectViewModel {

    static final System.Logger LOGGER = System.getLogger(ConnectViewModel.class.getName());

    ConfigService configService;
    MongoService mongoService;

    SimpleStringProperty name = new SimpleStringProperty();
    SimpleStringProperty connectionString = new SimpleStringProperty();
    SimpleStringProperty userName = new SimpleStringProperty();
    SimpleStringProperty password = new SimpleStringProperty();
    SimpleStringProperty database = new SimpleStringProperty();
    SimpleBooleanProperty testConnection = new SimpleBooleanProperty();

    @Inject
    public ConnectViewModel(ConfigService configService, MongoService mongoService) {
        this.configService = configService;
        this.mongoService = mongoService;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty connectionStringProperty() {
        return connectionString;
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty databaseProperty() {
        return database;
    }

    public BooleanProperty testConnectionProperty() {
        return testConnection;
    }

    void initFrom(Connection connection) {
        name.set(connection.name());
        userName.set(connection.username());
        password.set(connection.password());
        database.set(connection.database());
        connectionString.set(connection.servers());
    }

    public void loadConnection(String name) {
        initFrom(configService.getConnectionData(name));
    }

    public void saveConnection() {
        LOGGER.log(System.Logger.Level.INFO, "Calling save connection!{0}", this.name.get());
        configService.save(
                this.name.get(),
                connectionString.get(),
                userName.get(),
                password.get(),
                database.get());
    }

    public void testConnection() {
        LOGGER.log(System.Logger.Level.INFO, "Calling save connection!{0}", name.get());
        testConnection.set(mongoService.testConnection(
                connectionString.get(),
                userName.get(),
                password.get(),
                database.get()));
    }
}
