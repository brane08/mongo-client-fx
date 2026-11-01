package brane08.fx.mongo.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import brane08.fx.mongo.services.MongoService;
import brane08.fx.mongo.support.FxToolkitSupport;
import brane08.fx.mongo.support.TestFxModules;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GuiceFXMLLoaderTest {

    @BeforeAll
    static void initFx() {
        FxToolkitSupport.ensureStarted();
    }

    @Test
    void load_connectionFxml_returnsRoot() throws Exception {
        var loader = loaderOnFxThread();
        var root = new AtomicReference<>();
        runOnFx(() -> root.set(loader.get().load("connection.fxml")));
        assertNotNull(root.get());
    }

    @Test
    void load_mainFxml_returnsRoot() throws Exception {
        var loader = loaderOnFxThread();
        var root = new AtomicReference<>();
        runOnFx(() -> root.set(loader.get().load("main.fxml")));
        assertNotNull(root.get());
    }

    @Test
    void load_missingResource_throws() throws Exception {
        var loader = loaderOnFxThread();
        assertThrows(Exception.class, () -> runOnFx(() -> loader.get().load("does-not-exist.fxml")));
    }

    private static AtomicReference<GuiceFXMLLoader> loaderOnFxThread() throws Exception {
        var dir = Files.createTempDirectory("guice-fxml-test");
        var loader = new AtomicReference<GuiceFXMLLoader>();
        runOnFx(() -> {
            var stage = new Stage();
            var injector = TestFxModules.createInjector(dir, stage, mockMongo());
            loader.set(injector.getInstance(GuiceFXMLLoader.class));
        });
        return loader;
    }

    private static MongoService mockMongo() {
        return mock(MongoService.class);
    }

    private static void runOnFx(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        var done = new CountDownLatch(1);
        var error = new AtomicReference<Throwable>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                done.countDown();
            }
        });
        if (!done.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out on FX thread");
        }
        if (error.get() != null) {
            if (error.get() instanceof Exception e) {
                throw e;
            }
            if (error.get() instanceof Error e) {
                throw e;
            }
            throw new Exception(error.get());
        }
    }
}
