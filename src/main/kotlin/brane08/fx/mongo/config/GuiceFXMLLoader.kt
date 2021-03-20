package brane08.fx.mongo.config

import com.google.inject.Injector
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.util.Callback

class GuiceFXMLLoader(private val injector: Injector) {

    private val defaultLocation = "brane08/fx/mongo/";

    fun load(relativeFile: String): Parent {
        val loader = FXMLLoader()
        ClassLoader.getSystemResourceAsStream(defaultLocation + relativeFile).use { stream ->
            stream.let { istr ->
                loader.controllerFactory = Callback {
                    injector.getInstance(it)
                }
                return loader.load(istr)
            }
        }
    }
}
