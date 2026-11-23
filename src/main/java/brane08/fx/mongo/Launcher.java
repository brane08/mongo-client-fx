package brane08.fx.mongo;

/**
 * Entry point for the fat jar. The main class must not extend {@link javafx.application.Application}, otherwise the
 * JVM demands JavaFX on the module path when started with {@code java -jar}.
 */
public final class Launcher {

    private Launcher() {}

    public static void main(String[] args) {
        MongoApp.main(args);
    }
}
