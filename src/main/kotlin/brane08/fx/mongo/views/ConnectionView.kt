package brane08.fx.mongo.views

import brane08.fx.mongo.viewmodels.ConnectViewModel
import com.google.inject.Inject
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.kordamp.ikonli.javafx.FontIcon
import java.net.URL
import java.util.*

class ConnectionView @Inject constructor(private val model: ConnectViewModel) : Initializable {

    val showProgress = SimpleBooleanProperty(false)

    @FXML
    lateinit var nameTxt: TextField

    @FXML
    lateinit var serversTxt: TextField

    @FXML
    lateinit var dbTxt: TextField

    @FXML
    lateinit var userTxt: TextField

    @FXML
    lateinit var passwordTxt: TextField

    @FXML
    lateinit var testIcon: FontIcon

    @FXML
    lateinit var inProgress: ProgressIndicator

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        nameTxt.textProperty().bindBidirectional(model.nameProperty)
        serversTxt.textProperty().bindBidirectional(model.connectionStringProperty)
        dbTxt.textProperty().bindBidirectional(model.databaseProperty)
        userTxt.textProperty().bindBidirectional(model.userNameProperty)
        passwordTxt.textProperty().bindBidirectional(model.passwordProperty)

        inProgress.visibleProperty().bind(showProgress)
    }

    @FXML
    fun saveConnection() {
        showProgress.value = true
        println("Calling save connection: " + nameTxt.text)
        model.saveConnection()
        showProgress.value = false
    }

    @FXML
    fun testConnection() {
        showProgress.value = true
        println("Calling save connection: " + nameTxt.text)
        model.testConnection()
        showProgress.value = false
    }

    @FXML
    fun close(event: ActionEvent) {
        val stage: Stage = dbTxt.scene.window as Stage
        stage.close()
    }
}
