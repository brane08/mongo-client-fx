package brane08.fx.mongo.config

import brane08.fx.mongo.services.ConfigService
import brane08.fx.mongo.services.MongoService
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.stage.Stage
import javafx.util.Callback
import javax.inject.Qualifier
import javax.inject.Singleton

class FxAppModule : AbstractModule() {

    @Provides
    @Singleton
    fun mongoService(): MongoService {
        return MongoService()
    }

    @Provides
    @Singleton
    fun configService(): ConfigService {
        return ConfigService()
    }

    @Provides
    @Singleton
    fun fxmlLoader(injector: Injector): GuiceFXMLLoader {
        return GuiceFXMLLoader(injector)
    }
}

class FxStageModule(private val stage: Stage) : AbstractModule() {
    override fun configure() {
        bind(Stage::class.java).annotatedWith(RootStage::class.java).toInstance(stage)
    }
}

class GuiceFXMLLoader(private val injector: Injector) {

    private val defaultLocation = "brane08/fx/mongo/";

    fun load(relativeFile: String): Parent {
        val loader = FXMLLoader()
        ClassLoader.getSystemResourceAsStream(defaultLocation + relativeFile).use { stream ->
            stream.let { istr ->
                loader.controllerFactory = Callback {
                    injector.getInstance(it)
                }
                return loader.load(istr)
            }
        }
    }
}

@Qualifier
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RootStage {

}
