package brane08.fx.mongo.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

import brane08.fx.mongo.config.GuiceFXMLLoader;
import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.support.TestFxModules;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectionViewTest {

    @TempDir
    static Path configDir;

    Stage stage;
    GuiceFXMLLoader fxmlLoader;
    ConfigService configService;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        var injector = TestFxModules.createInjector(configDir, stage);
        fxmlLoader = injector.getInstance(GuiceFXMLLoader.class);
        configService = injector.getInstance(ConfigService.class);
        showConnectionScene();
    }

    @BeforeEach
    void resetScene() throws IOException {
        Files.deleteIfExists(configDir.resolve("connections.conf"));
        Files.deleteIfExists(configDir.resolve(".key"));
        WaitForAsyncUtils.waitForFxEvents();
        if (stage != null && fxmlLoader != null) {
            robotShow();
        }
    }

    private void robotShow() {
        WaitForAsyncUtils.asyncFx(this::showConnectionScene);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void showConnectionScene() {
        stage.setScene(new Scene(fxmlLoader.load("connection.fxml")));
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    @Test
    @Order(1)
    void saveConnection_persistsEncryptedPassword(FxRobot robot) throws IOException {
        robot.clickOn("#nameTxt").write("prod");
        robot.clickOn("#serversTxt").write("cluster.example.net");
        robot.clickOn("#dbTxt").write("admin");
        robot.clickOn("#userTxt").write("appuser");
        robot.clickOn("#passwordTxt").write("s3cret");

        verifyThat("#nameTxt", hasText("prod"));
        robot.clickOn("Save");

        assertEquals("prod", configService.getConnectionData("prod").name());
        assertEquals("s3cret", configService.getConnectionData("prod").password());
        assertTrue(Files.readString(configDir.resolve("connections.conf")).contains("ENC:"));
        assertEquals("prod", robot.lookup("#nameTxt").queryAs(TextField.class).getText());
    }

    @Test
    @Order(2)
    void testConnection_invokesWithoutError(FxRobot robot) {
        robot.clickOn("#nameTxt").write("prod");
        robot.clickOn("#serversTxt").write("cluster.example.net");
        robot.clickOn("#dbTxt").write("admin");
        robot.clickOn("#userTxt").write("appuser");
        robot.clickOn("#passwordTxt").write("s3cret");
        robot.clickOn("Test Connection");
        verifyThat("#nameTxt", hasText("prod"));
    }

    @Test
    @Order(3)
    void cancel_closesWindow(FxRobot robot) {
        assertTrue(stage.isShowing());
        robot.clickOn("Cancel");
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(stage.isShowing());
    }
}
