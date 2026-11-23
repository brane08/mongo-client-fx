package brane08.fx.mongo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.application.Application;
import org.junit.jupiter.api.Test;

class PackagingTest {

    @Test
    void launcher_isNotAnApplication_soFatJarCanStart() {
        assertFalse(Application.class.isAssignableFrom(Launcher.class));
    }

    @Test
    void systemLogger_isRoutedThroughSlf4j() {
        var logger = System.getLogger("packaging.test");
        assertTrue(logger.getClass().getName().startsWith("org.slf4j"), logger.getClass().getName());
    }
}
