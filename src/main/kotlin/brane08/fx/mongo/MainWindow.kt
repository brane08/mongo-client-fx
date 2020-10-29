package brane08.fx.mongo

import brane08.fx.mongo.connect.ConnectView
import javafx.geometry.Rectangle2D
import javafx.stage.Screen
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.*


class MainWindow : View() {
    override val root = borderpane {
        top {
            menubar {
                menu("File") {
                    item("Connect").action {
                        openInternalWindow<ConnectView>()
                    }
                    item("Quit")
                }
                menu("Edit") {
                    item("Copy")
                    item("Paste")
                }
                menu("Help") {
                    item("About")
                }
            }
        }
        center {
            hbox {
                spacing = 10.0
                paddingAll = 10
                button("Press me")
                label("Waiting")
            }
        }
    }

    companion object {

        val LOG: Logger = LoggerFactory.getLogger("take-break")
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
