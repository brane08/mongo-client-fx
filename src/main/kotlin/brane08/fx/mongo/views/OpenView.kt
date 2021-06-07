package brane08.fx.mongo.views

import brane08.fx.mongo.viewmodels.ConnectViewModel
import brane08.fx.mongo.viewmodels.OpenViewModel
import com.google.inject.Inject
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import javafx.scene.text.Text
import java.net.URL
import java.util.*

class OpenView @Inject constructor(
    private val model: OpenViewModel,
    private val connectVM: ConnectViewModel
) : Initializable {

    @FXML
    lateinit var name: Text

    @FXML
    lateinit var server: Text

    @FXML
    lateinit var user: Text

    @FXML
    lateinit var database: Text

    @FXML
    lateinit var connections: ListView<String>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        connections.items = model.getNames()
        if (connections.items.size > 0) {
            connections.selectionModel.select(0)
        }

        connections.selectionModel.selectedItemProperty()
            .addListener { list: ObservableValue<out String>, oldVal: String, newVal: String ->
                connectVM.loadConnection(newVal)
            }
        name.textProperty().bind(connectVM.nameProperty)
        server.textProperty().bind(connectVM.connectionStringProperty)
        user.textProperty().bind(connectVM.userNameProperty)
        database.textProperty().bind(connectVM.databaseProperty)
        connectVM.loadConnection(connections.selectionModel.selectedItem)
    }
}
