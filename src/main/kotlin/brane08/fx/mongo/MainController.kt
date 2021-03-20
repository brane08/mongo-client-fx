package brane08.fx.mongo

import brane08.fx.mongo.config.GuiceFXMLLoader
import com.google.inject.Inject
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage

class MainController @Inject constructor(private val fxmlLoader: GuiceFXMLLoader) {

    fun openConnectWindow(event: ActionEvent) {
        val root = fxmlLoader.load("connect.fxml")
        val stage = Stage()
        stage.title = "Manage Connections"
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.scene = Scene(root)
        stage.show()
    }

    companion object {

        private var parentStage: Stage? = null

        fun setParentStage(stage: Stage) {
            parentStage = stage
        }

        fun getParentStage(): Stage? {
            return parentStage
        }
    }
}
