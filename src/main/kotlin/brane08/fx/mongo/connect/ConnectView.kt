package brane08.fx.mongo.connect

import javafx.geometry.Orientation
import javafx.scene.Parent
import tornadofx.*

class ConnectView : View() {

    private val controller: ConnectController by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()
    }

    override val root = vbox {
        title = "Manage Connections"
        hbox {
            vbox {
                listview(controller.getNames()) {
                    selectionModel.selectedItemProperty().onChange {
                        if (it != null) {
                            controller.getConnection(it)
                        }
                    }
                }
            }
            form {
                minWidth = 480.0

                fieldset("Connection Details", labelPosition = Orientation.VERTICAL) {
                    field("Name") {
                        textfield(controller.model.nameProperty)
                    }
                    field("Servers List (comma separated <server>:<port>)") {
                        textfield(controller.model.connectionStringProperty)
                    }
                    field("Database") {
                        textfield(controller.model.databaseProperty)
                    }
                    field("User Name") {
                        textfield(controller.model.userNameProperty)
                    }
                    field("Password") {
                        passwordfield(controller.model.passwordProperty)
                    }
                }
                hbox {
                    spacing = 10.0
                    button("Save") {
                        action { controller.saveConnection() }
                    }
                    button("Test Connection") {
                        action {
                            controller.testConnection()
                        }
                    }

                    label(controller.model.testConnectionProperty) { }
                }
            }
        }
    }
}