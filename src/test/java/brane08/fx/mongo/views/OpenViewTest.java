package brane08.fx.mongo.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

import brane08.fx.mongo.config.GuiceFXMLLoader;
import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.support.TestFxModules;
import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class OpenViewTest {

    @TempDir
    static Path configDir;

    @Start
    void start(Stage stage) {
        var injector = TestFxModules.createInjector(configDir, stage);
        var configService = injector.getInstance(ConfigService.class);
        configService.save("alpha", "alpha.example.net", "alpha-user", "alpha-pass", "alpha-db");
        configService.save("beta", "beta.example.net", "beta-user", "beta-pass", "beta-db");

        var fxmlLoader = injector.getInstance(GuiceFXMLLoader.class);
        stage.setScene(new Scene(fxmlLoader.load("open.fxml")));
        stage.show();
    }

    @Test
    void selectingSecondConnection_updatesBoundDetails(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        @SuppressWarnings("unchecked")
        var list = (ListView<String>) robot.lookup("#connections").query();
        assertEquals(2, list.getItems().size());

        robot.interact(() -> list.getSelectionModel().select("beta"));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("beta", robot.lookup("#name").queryAs(Text.class).getText());
        assertEquals("beta.example.net", robot.lookup("#server").queryAs(Text.class).getText());
        assertEquals("beta-user", robot.lookup("#user").queryAs(Text.class).getText());
        assertEquals("beta-db", robot.lookup("#database").queryAs(Text.class).getText());
        verifyThat("#name", NodeMatchers.isVisible());
    }
}
