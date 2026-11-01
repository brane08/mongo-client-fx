package brane08.fx.mongo;

import brane08.fx.mongo.config.FxAppModule;
import brane08.fx.mongo.config.FxStageModule;
import brane08.fx.mongo.config.GuiceFXMLLoader;
import com.google.inject.Guice;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MongoApp extends Application {

    static final int SCALE_FACTOR = 70;
    static double WIDTH;
    static double HEIGHT;
    static double X;
    static double Y;

    static {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        WIDTH = (screenBounds.getWidth() * SCALE_FACTOR) / 100;
        HEIGHT = (screenBounds.getHeight() * SCALE_FACTOR) / 100;
        X = (screenBounds.getWidth() - WIDTH) / 2;
        Y = (screenBounds.getHeight() - HEIGHT) / 2;
    }

    @Override
    public void start(Stage stage) {
        var guice = Guice.createInjector(new FxAppModule(), new FxStageModule(stage));
        var fxmlLoader = guice.getInstance(GuiceFXMLLoader.class);
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setX(X);
        stage.setY(Y);
        stage.setScene(new Scene(fxmlLoader.load("main.fxml")));
        stage.show();
    }

    public static void main(String[] args) {
        launch(MongoApp.class);
    }
}
