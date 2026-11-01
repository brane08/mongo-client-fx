package brane08.fx.mongo.support;

import brane08.fx.mongo.config.FxAppModule;
import brane08.fx.mongo.config.FxStageModule;
import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.services.MongoService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.util.Modules;
import jakarta.inject.Singleton;
import java.nio.file.Path;
import javafx.stage.Stage;
import org.mockito.Mockito;

public final class TestFxModules {

    private TestFxModules() {}

    public static Injector createInjector(Path configDir, Stage rootStage) {
        return createInjector(configDir, rootStage, Mockito.mock(MongoService.class));
    }

    public static Injector createInjector(Path configDir, Stage rootStage, MongoService mongoService) {
        return Guice.createInjector(
                Modules.override(new FxAppModule()).with(new AbstractModule() {
                    @Provides
                    @Singleton
                    ConfigService configService() {
                        return new ConfigService(configDir);
                    }

                    @Provides
                    @Singleton
                    MongoService mongoService() {
                        return mongoService;
                    }
                }),
                new FxStageModule(rootStage));
    }
}
