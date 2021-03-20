package brane08.fx.mongo.connect

import brane08.fx.mongo.config.ConfigService
import brane08.fx.mongo.services.MongoServices
import com.google.inject.Inject
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

class ConnectController @Inject constructor(
    private val configService: ConfigService,
    private val mongoServices: MongoServices
) : Initializable {

    @FXML
    lateinit var connections: ListView<String>

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    var model: ConnectModel = ConnectModel()

    fun getConnection(name: String) {
        val connectionData = configService.getConnectionData(name)
        if (connectionData != null)
            model.initFrom(connectionData)
    }

    fun saveConnection() {
        println("Calling save connection!")
        configService.save(
            model.nameProperty.value, model.connectionStringProperty.value, model.userNameProperty.value,
            model.passwordProperty.value, model.databaseProperty.value
        )
    }

    fun getNames(): ObservableList<String> {
        return FXCollections.observableArrayList(configService.getConnectionNames())
    }

    fun testConnection() {
        model.testConnectionProperty.value =
            mongoServices.testConnection(
                model.connectionStringProperty.value,
                model.userNameProperty.value,
                model.passwordProperty.value,
                model.databaseProperty.value
            )
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        connections.items = getNames()
    }
}
