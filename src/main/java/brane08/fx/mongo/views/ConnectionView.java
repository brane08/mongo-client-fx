package brane08.fx.mongo.views;

import brane08.fx.mongo.viewmodels.ConnectViewModel;
import jakarta.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class ConnectionView implements Initializable {

    static final System.Logger LOGGER = System.getLogger(ConnectionView.class.getName());

    ConnectViewModel model;
    SimpleBooleanProperty showProgress = new SimpleBooleanProperty(false);

    @FXML
    TextField nameTxt;
    @FXML
    TextField serversTxt;
    @FXML
    TextField dbTxt;
    @FXML
    TextField userTxt;
    @FXML
    PasswordField passwordTxt;
    @FXML
    FontIcon testIcon;
    @FXML
    ProgressIndicator inProgress;

    @Inject
    public ConnectionView(ConnectViewModel model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameTxt.textProperty().bindBidirectional(model.nameProperty());
        serversTxt.textProperty().bindBidirectional(model.connectionStringProperty());
        dbTxt.textProperty().bindBidirectional(model.databaseProperty());
        userTxt.textProperty().bindBidirectional(model.userNameProperty());
        passwordTxt.textProperty().bindBidirectional(model.passwordProperty());
        inProgress.visibleProperty().bind(showProgress);
    }

    @FXML
    public void saveConnection() {
        showProgress.set(true);
        LOGGER.log(System.Logger.Level.INFO, "Calling save connection: {0}", nameTxt.getText());
        model.saveConnection();
        showProgress.set(false);
    }

    @FXML
    public void testConnection() {
        if (showProgress.get()) {
            return;
        }
        showProgress.set(true);
        LOGGER.log(System.Logger.Level.INFO, "Calling test connection: {0}", nameTxt.getText());
        var probe = model.prepareTest();
        var task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                return probe.get();
            }
        };
        task.setOnSucceeded(e -> {
            model.testConnectionProperty().set(task.getValue());
            showProgress.set(false);
        });
        task.setOnFailed(e -> {
            LOGGER.log(System.Logger.Level.ERROR, "Connection test failed", task.getException());
            model.testConnectionProperty().set(false);
            showProgress.set(false);
        });
        var thread = new Thread(task, "mongo-connection-test");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void close(ActionEvent event) {
        var stage = (Stage) dbTxt.getScene().getWindow();
        stage.close();
    }
}
