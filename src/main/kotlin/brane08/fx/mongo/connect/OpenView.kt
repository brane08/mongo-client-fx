package brane08.fx.mongo.connect

import com.google.inject.Inject
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import java.net.URL
import java.util.*

class OpenView @Inject constructor(private val model: OpenViewModel) : Initializable {

    @FXML
    lateinit var connections: ListView<String>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        connections.items = model.getNames()
        if (connections.items.size > 0) {
            connections.selectionModel.select(0)
        }
    }
}
