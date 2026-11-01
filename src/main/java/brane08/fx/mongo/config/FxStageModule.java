package brane08.fx.mongo.config;

import com.google.inject.AbstractModule;
import javafx.stage.Stage;

public class FxStageModule extends AbstractModule {

    Stage stage;

    public FxStageModule(Stage stage) {
        this.stage = stage;
    }

    @Override
    protected void configure() {
        bind(Stage.class).annotatedWith(RootStage.class).toInstance(stage);
    }
}
