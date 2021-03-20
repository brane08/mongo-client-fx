package brane08.fx.mongo

import brane08.fx.mongo.config.GuiceFXMLLoader
import brane08.fx.mongo.di.FxAppModule
import com.google.inject.Guice
import javafx.application.Application
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.stage.Screen
import javafx.stage.Stage


class MongoApp : Application() {

    override fun start(stage: Stage) {
        val guice = Guice.createInjector(FxAppModule())
        val fxmlLoader = guice.getInstance(GuiceFXMLLoader::class.java)
        with(stage) {
            minWidth = WIDTH
            minHeight = HEIGHT
            x = X
            y = Y
            stage.scene = Scene(fxmlLoader.load("main.fxml"))
            stage.show()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MongoApp::class.java)
        }

        private const val SCALE_FACTOR = 70
        var WIDTH: Double = 0.0
        var HEIGHT: Double = 0.0
        var X = 0.0
        var Y = 0.0

        init {
            val screenBounds: Rectangle2D = Screen.getPrimary().visualBounds
            WIDTH = (screenBounds.width * SCALE_FACTOR) / 100
            HEIGHT = (screenBounds.height * SCALE_FACTOR) / 100
            X = (screenBounds.width - WIDTH) / 2;
            Y = (screenBounds.height - HEIGHT) / 2;
        }
    }
}


