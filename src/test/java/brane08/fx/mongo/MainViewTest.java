package brane08.fx.mongo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import brane08.fx.mongo.config.GuiceFXMLLoader;
import brane08.fx.mongo.support.TestFxModules;
import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class MainViewTest {

    @TempDir
    static Path configDir;

    @Start
    void start(Stage stage) {
        injector = TestFxModules.createInjector(configDir, stage);
        var fxmlLoader = injector.getInstance(GuiceFXMLLoader.class);
        stage.setScene(new Scene(fxmlLoader.load("main.fxml")));
        stage.setTitle("Mongo Client");
        stage.show();
    }

    @Test
    void connectionNew_opensManageConnectionsDialog(FxRobot robot) {
        robot.clickOn("Connection").clickOn("New");
        WaitForAsyncUtils.waitForFxEvents();

        var windows = robot.listWindows();
        assertTrue(windows.size() >= 2, "expected main stage plus manage-connections dialog");

        var dialog = windows.stream()
                .filter(window -> window instanceof Stage stage
                        && "Manage Connections".equals(stage.getTitle()))
                .map(Stage.class::cast)
                .findFirst();
        assertTrue(dialog.isPresent());

        robot.targetWindow(dialog.get());
        WaitForAsyncUtils.waitForFxEvents();
        var nameTxt = robot.lookup("#nameTxt").queryAs(TextField.class);
        assertNotNull(nameTxt);
        verifyThat("#nameTxt", isVisible());
        assertTrue(robot.lookup("Connection Details").tryQuery().isPresent());
    }

    @Test
    void connectionOpen_opensOpenConnectionDialog(FxRobot robot) {
        injector.getInstance(brane08.fx.mongo.services.ConfigService.class)
                .save("seed", "seed.example.net", "u", "p", "db");

        robot.clickOn("Connection").clickOn("Open");
        WaitForAsyncUtils.waitForFxEvents();

        var dialog = robot.listWindows().stream()
                .filter(window -> window instanceof Stage stage
                        && "Open Connection".equals(stage.getTitle()))
                .map(Stage.class::cast)
                .findFirst();
        assertTrue(dialog.isPresent());
        robot.targetWindow(dialog.get());
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(robot.lookup("#connections").query());
    }

    private com.google.inject.Injector injector;
}
