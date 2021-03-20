package brane08.fx.mongo.connect

import brane08.fx.mongo.config.Connection
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty

class ConnectModel(
    id: Int = 0,
    name: String = "",
    connectionString: String = "",
    userName: String = "",
    password: String = "",
    database: String = ""
) {

    val idProperty = SimpleIntegerProperty(id)

    val nameProperty = SimpleStringProperty(name)

    val connectionStringProperty = SimpleStringProperty(connectionString)

    val userNameProperty = SimpleStringProperty(userName)

    val passwordProperty = SimpleStringProperty(password)

    val databaseProperty = SimpleStringProperty(database)

    val testConnectionProperty = SimpleBooleanProperty(false)

    fun initFrom(connection: Connection) {
        this.idProperty.value = connection.id.value
        this.nameProperty.value = connection.name
        this.userNameProperty.value = connection.username
        this.passwordProperty.value = connection.password
        this.databaseProperty.value = connection.database
        this.connectionStringProperty.value = connection.servers
    }
}


