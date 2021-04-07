package brane08.fx.mongo.services

import brane08.fx.mongo.config.Connection
import brane08.fx.mongo.config.Connections
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class ConfigService {

    private val db: Database = Database.connect(CONNECTION_STRING, DRIVER, USERNAME, PASSWORD)

    init {
        // transaction { org.jetbrains.exposed.sql.SchemaUtils.create(Connections) }
    }

    fun getConnectionData(name: String): Connection {
        return transaction { Connection.find { Connections.name eq name }.limit(1).first() }
    }

    fun getConnectionNames(): List<String> {
        return transaction { Connection.all().map { it.name } }
    }

    fun save(cName: String, connectionString: String, userName: String, cPassword: String, cDatabase: String) {
        transaction {
            Connection.new {
                name = cName
                servers = connectionString
                username = userName
                password = cPassword
                database = cDatabase
            }
        }
    }

    companion object {
        private const val CONNECTION_STRING = "jdbc:h2:~/mongo-fx"
        private const val USERNAME = "mongofx"
        private const val PASSWORD = "password"
        private const val DRIVER = "org.h2.Driver"
    }
}
