package brane08.fx.mongo.config;

import brane08.fx.mongo.ApplicationException;
import com.google.inject.Injector;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class GuiceFXMLLoader {

    static final String FXML_PREFIX = "brane08/fx/mongo/";

    Injector injector;

    public GuiceFXMLLoader(Injector injector) {
        this.injector = injector;
    }

    public Parent load(String relativeFile) {
        var loader = new FXMLLoader();
        loader.setControllerFactory(injector::getInstance);
        try (var stream = ClassLoader.getSystemResourceAsStream(FXML_PREFIX + relativeFile)) {
            return loader.load(stream);
        } catch (IOException e) {
            throw new ApplicationException("Unable to load FXML: " + relativeFile, e);
        }
    }
}
