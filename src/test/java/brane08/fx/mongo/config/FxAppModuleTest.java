package brane08.fx.mongo.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.services.MongoService;
import com.google.inject.Guice;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FxAppModuleTest {

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

    @Test
    void providesSingletons() {
        var injector = Guice.createInjector(new FxAppModule());

        assertNotNull(injector.getInstance(MongoService.class));
        assertSame(injector.getInstance(MongoService.class), injector.getInstance(MongoService.class));
        assertNotNull(injector.getInstance(ConfigService.class));
        assertSame(injector.getInstance(ConfigService.class), injector.getInstance(ConfigService.class));
        assertNotNull(injector.getInstance(GuiceFXMLLoader.class));
    }
}
