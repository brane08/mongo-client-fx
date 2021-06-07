package brane08.fx.mongo.viewmodels

import brane08.fx.mongo.config.Connection
import brane08.fx.mongo.services.ConfigService
import brane08.fx.mongo.services.MongoService
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javax.inject.Inject

class ConnectViewModel @Inject constructor(
    private val configService: ConfigService,
    private val mongoService: MongoService
) {

    val idProperty = SimpleIntegerProperty()

    val nameProperty = SimpleStringProperty()

    val connectionStringProperty = SimpleStringProperty()

    val userNameProperty = SimpleStringProperty()

    val passwordProperty = SimpleStringProperty()

    val databaseProperty = SimpleStringProperty()

    val testConnectionProperty = SimpleBooleanProperty()

    private fun initFrom(connection: Connection) {
        this.idProperty.value = connection.id.value
        this.nameProperty.value = connection.name
        this.userNameProperty.value = connection.username
        this.passwordProperty.value = connection.password
        this.databaseProperty.value = connection.database
        this.connectionStringProperty.value = connection.servers
    }

    fun loadConnection(name: String) {
        val connectionData = configService.getConnectionData(name)
        initFrom(connectionData)
    }

    fun saveConnection() {
        println("Calling save connection!" + nameProperty.value)
        configService.save(
            nameProperty.value, connectionStringProperty.value, userNameProperty.value,
            passwordProperty.value, databaseProperty.value
        )
    }

    fun testConnection() {
        println("Calling save connection!" + nameProperty.value)
        testConnectionProperty.value =
            mongoService.testConnection(
                connectionStringProperty.value,
                userNameProperty.value,
                passwordProperty.value,
                databaseProperty.value
            )
    }
}


