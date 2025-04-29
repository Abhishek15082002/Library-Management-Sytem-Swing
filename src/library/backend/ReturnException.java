package library.backend;

/**
 * Custom exception for return-related business rule failures.
 */
public class ReturnException extends Exception { // Make it public
    public ReturnException(String message) {
        super(message);
    }
}
