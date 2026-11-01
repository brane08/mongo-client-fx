package brane08.fx.mongo.viewmodels;

import brane08.fx.mongo.services.ConfigService;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OpenViewModel {

    ConfigService configService;

    @Inject
    public OpenViewModel(ConfigService configService) {
        this.configService = configService;
    }

    public ObservableList<String> getNames() {
        return FXCollections.observableArrayList(configService.getConnectionNames());
    }
}
