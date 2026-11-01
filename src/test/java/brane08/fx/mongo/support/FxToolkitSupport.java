package brane08.fx.mongo.support;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;

public final class FxToolkitSupport {

    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private FxToolkitSupport() {}

    public static void ensureStarted() {
        if (STARTED.get()) {
            return;
        }
        synchronized (FxToolkitSupport.class) {
            if (STARTED.get()) {
                return;
            }
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException alreadyStarted) {
                // Toolkit already running in this JVM.
            }
            STARTED.set(true);
        }
    }
}
