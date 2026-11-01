package brane08.fx.mongo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class MongoAppTest {

    @TempDir
    Path home;

    String previousHome;

    @BeforeEach
    void redirectHome() {
        previousHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
    }

    @AfterEach
    void restoreHome() {
        if (previousHome == null) {
            System.clearProperty("user.home");
        } else {
            System.setProperty("user.home", previousHome);
        }
    }

    @Start
    void start(Stage stage) {
        new MongoApp().start(stage);
    }

    @Test
    void start_showsMainWindow(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(MongoApp.WIDTH > 0);
        assertTrue(MongoApp.HEIGHT > 0);
        assertNotNull(robot.lookup("Connection").query());
        assertNotNull(robot.lookup("Press Here!").query());
    }
}
