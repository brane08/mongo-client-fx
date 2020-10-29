package brane08.fx.mongo.connect

import brane08.fx.mongo.config.ConfigService
import brane08.fx.mongo.services.MongoServices
import javafx.collections.ObservableList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.Controller
import tornadofx.asObservable

class ConnectController : Controller() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    var model: ConnectModel = ConnectModel()
    private val configService: ConfigService by di()
    private val mongoServices: MongoServices by di()

    fun getConnection(name: String) {

        val connectionData = configService.getConnectionData(name)
        if (connectionData != null)
            model.initFrom(connectionData)
    }

    fun saveConnection() {
        println("Calling save connection!")
        configService.save(model.name, model.connectionString, model.userName, model.password, model.database)
    }

    fun getNames(): ObservableList<String> {
        return configService.getConnectionNames().asObservable()
    }

    fun testConnection() {
        model.testConnection =
            mongoServices.testConnection(model.connectionString, model.userName, model.password, model.database)
    }
}
