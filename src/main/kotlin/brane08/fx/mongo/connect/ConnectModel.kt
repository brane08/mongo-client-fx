package brane08.fx.mongo.connect

import brane08.fx.mongo.config.Connection
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*;

class ConnectModel(
    id: Int = 0,
    name: String = "",
    connectionString: String = "",
    userName: String = "",
    password: String = "",
    database: String = ""
) {

    val idProperty = SimpleIntegerProperty(id)
    var id by idProperty

    val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty

    val connectionStringProperty = SimpleStringProperty(connectionString)
    var connectionString: String by connectionStringProperty

    val userNameProperty = SimpleStringProperty(userName)
    var userName: String by userNameProperty

    val passwordProperty = SimpleStringProperty(password)
    var password: String by passwordProperty

    val databaseProperty = SimpleStringProperty(database)
    var database: String by databaseProperty

    val testConnectionProperty = SimpleBooleanProperty(false)
    var testConnection by testConnectionProperty

    fun initFrom(connection: Connection) {
        this.idProperty.value = connection.id.value
        this.nameProperty.value = connection.name
        this.userNameProperty.value = connection.username
        this.passwordProperty.value = connection.password
        this.databaseProperty.value = connection.database
        this.connectionStringProperty.value = connection.servers
    }
}


