package brane08.fx.mongo

class ApplicationException : RuntimeException {

    constructor(message: String) : super(message)
    constructor(message: String, exception: Exception) : super(message, exception)
}
