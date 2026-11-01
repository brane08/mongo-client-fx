package brane08.fx.mongo;

import brane08.fx.mongo.config.GuiceFXMLLoader;
import brane08.fx.mongo.config.RootStage;
import brane08.fx.mongo.services.ConfigService;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainView implements Initializable {

    GuiceFXMLLoader fxmlLoader;
    Stage rootStage;
    ConfigService configService;

    @FXML
    Menu newMenu;

    @Inject
    public MainView(GuiceFXMLLoader fxmlLoader, @RootStage Stage rootStage, ConfigService configService) {
        this.fxmlLoader = fxmlLoader;
        this.rootStage = rootStage;
        this.configService = configService;
    }

    @FXML
    public void openConnectWindow(ActionEvent event) {
        var stage = new Stage();
        stage.setTitle("Manage Connections");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(rootStage);
        stage.setScene(new Scene(fxmlLoader.load("connection.fxml")));
        stage.show();
    }

    void refreshNames() {
        newMenu.getItems().clear();
        configService.getConnectionNames().forEach(name -> {
            var menu = new MenuItem(name);
            menu.getOnAction();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void openConnection(ActionEvent event) {
        var stage = new Stage();
        stage.setTitle("Open Connection");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(rootStage);
        stage.setScene(new Scene(fxmlLoader.load("open.fxml")));
        stage.show();
    }
}
