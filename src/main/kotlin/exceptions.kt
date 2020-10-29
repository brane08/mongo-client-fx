import java.lang.Exception
import java.lang.RuntimeException

class ApplicationException : RuntimeException {

    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
}