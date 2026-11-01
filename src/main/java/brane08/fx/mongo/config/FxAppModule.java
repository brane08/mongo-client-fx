package brane08.fx.mongo.config;

import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.services.MongoService;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import jakarta.inject.Singleton;

public class FxAppModule extends AbstractModule {

    @Provides
    @Singleton
    MongoService mongoService() {
        return new MongoService();
    }

    @Provides
    @Singleton
    ConfigService configService() {
        return new ConfigService();
    }

    @Provides
    @Singleton
    GuiceFXMLLoader fxmlLoader(Injector injector) {
        return new GuiceFXMLLoader(injector);
    }
}
