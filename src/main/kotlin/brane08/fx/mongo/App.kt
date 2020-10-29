package brane08.fx.mongo

import brane08.fx.mongo.di.FxAppModule
import com.google.inject.Guice
import javafx.stage.Stage
import tornadofx.*
import kotlin.reflect.KClass


class Application : App(MainWindow::class, AppStyle::class) {
    init {
        val guice = Guice.createInjector(FxAppModule())
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>) = guice.getInstance(type.java)
        }
    }

    override fun start(stage: Stage) {

        with(stage) {
            minWidth = MainWindow.WIDTH
            minHeight = MainWindow.HEIGHT
            x = MainWindow.X
            y = MainWindow.Y
            super.start(this)
        }
    }
}

fun main(args: Array<String>) {
    launch<Application>(args)
}

