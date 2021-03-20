package brane08.fx.mongo.connect

import javafx.util.StringConverter

class StringBooleanConverter : StringConverter<Boolean>() {
    override fun toString(value: Boolean): String {
        return value.toString()
    }

    override fun fromString(value: String): Boolean {
        return "true".equals(value, true)
    }

}
