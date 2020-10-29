package brane08.fx.mongo.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


class ConfigService {
    private val connectionString = "jdbc:h2:~/mongo-fx"
    private val username = "mongofx"
    private val password = "password"

    private val db: Database

    init {
        db = Database.connect(connectionString, "org.h2.Driver", username, password)
//        transaction { SchemaUtils.create(Connections) }

    }

    fun getConnectionData(name: String): Connection? {
        return transaction { Connection.find { Connections.name eq name }.limit(1).firstOrNull() }
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
}
