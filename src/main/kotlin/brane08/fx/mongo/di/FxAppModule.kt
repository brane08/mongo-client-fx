package brane08.fx.mongo.di

import brane08.fx.mongo.config.ConfigService
import brane08.fx.mongo.services.MongoServices
import com.google.inject.AbstractModule
import com.google.inject.Provides

class FxAppModule : AbstractModule() {

    @Provides
    fun mongoServices(): MongoServices {
        return MongoServices()
    }

    @Provides
    fun configService(): ConfigService {
        return ConfigService()
    }

}
