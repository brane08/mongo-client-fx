package brane08.fx.mongo.views;

import brane08.fx.mongo.viewmodels.ConnectViewModel;
import brane08.fx.mongo.viewmodels.OpenViewModel;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class OpenView implements Initializable {

    OpenViewModel model;
    ConnectViewModel connectVM;

    @FXML
    Text name;
    @FXML
    Text server;
    @FXML
    Text user;
    @FXML
    Text database;
    @FXML
    ListView<String> connections;

    @Inject
    public OpenView(OpenViewModel model, ConnectViewModel connectVM) {
        this.model = model;
        this.connectVM = connectVM;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connections.setItems(model.getNames());
        if (connections.getItems().size() > 0) {
            connections.getSelectionModel().select(0);
        }
        connections.getSelectionModel().selectedItemProperty()
                .addListener((_, _, newVal) -> connectVM.loadConnection(newVal));
        name.textProperty().bind(connectVM.nameProperty());
        server.textProperty().bind(connectVM.connectionStringProperty());
        user.textProperty().bind(connectVM.userNameProperty());
        database.textProperty().bind(connectVM.databaseProperty());
        connectVM.loadConnection(connections.getSelectionModel().getSelectedItem());
    }
}
