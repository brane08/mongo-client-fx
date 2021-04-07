package brane08.fx.mongo

import brane08.fx.mongo.config.GuiceFXMLLoader
import brane08.fx.mongo.config.RootStage
import brane08.fx.mongo.services.ConfigService
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.stage.Modality
import javafx.stage.Stage
import java.net.URL
import java.util.*
import javax.inject.Inject

class MainView @Inject constructor(
    private val fxmlLoader: GuiceFXMLLoader,
    @RootStage private val rootStage: Stage,
    private val configService: ConfigService
) : Initializable {

    @FXML
    lateinit var newMenu: Menu

    fun openConnectWindow(event: ActionEvent) {
        val root = fxmlLoader.load("connection.fxml")
        val stage = Stage()
        stage.title = "Manage Connections"
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(rootStage)
        stage.scene = Scene(root)
        stage.show()
    }

    private fun refreshNames() {
        newMenu.items.clear()
        configService.getConnectionNames().forEach {
            val menu = MenuItem(it)
            menu.onAction
        }
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
    }

    fun openConnection(event: ActionEvent) {
        val root = fxmlLoader.load("open.fxml")
        val stage = Stage()
        stage.title = "Open Connection"
        stage.initModality(Modality.WINDOW_MODAL)
        stage.initOwner(rootStage)
        stage.scene = Scene(root)
        stage.show()
    }
}
