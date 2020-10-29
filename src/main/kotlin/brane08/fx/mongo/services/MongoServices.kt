package brane08.fx.mongo.services

import brane08.fx.mongo.config.Connection
import com.mongodb.MongoException
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

class MongoServices {

    fun execute(): String {
        TODO()
    }

    private fun getClient(connection: Connection): MongoClient {
        return getClient(connection.servers, connection.username, connection.password, connection.database);
    }

    private fun getClient(servers: String, userName: String, password: String, database: String): MongoClient {
        val url = "mongodb://${userName}:${password}@${servers}" + "/${database}?authSource=admin"
        return MongoClients.create(url);
    }

    fun testConnection(connection: Connection): Boolean {
        return testConnection(connection.servers, connection.servers, connection.password, connection.database)
    }

    fun testConnection(servers: String, userName: String, password: String, database: String): Boolean {
        try {
            val client: MongoClient = getClient(servers, userName, password, database);
            val mongoDatabase = client.getDatabase(database)
            print("Collections: " + mongoDatabase.listCollectionNames().count())
            client.close()
        } catch (ex: MongoException) {
            ex.printStackTrace()
            return false
        }
        return true
    }
}
