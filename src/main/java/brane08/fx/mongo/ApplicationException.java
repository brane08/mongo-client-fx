package brane08.fx.mongo;

/** Unchecked exception for failures the application reports to the user. */
public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
