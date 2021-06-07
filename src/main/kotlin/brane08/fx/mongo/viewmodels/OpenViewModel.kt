package brane08.fx.mongo.viewmodels

import brane08.fx.mongo.services.ConfigService
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javax.inject.Inject

class OpenViewModel @Inject constructor(private val configService: ConfigService) {

    fun getNames(): ObservableList<String> {
        return FXCollections.observableArrayList(configService.getConnectionNames())
    }
}
