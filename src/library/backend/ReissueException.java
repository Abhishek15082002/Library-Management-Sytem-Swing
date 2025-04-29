package library.backend;

/**
 * Custom exception for reissue-related business rule failures.
 */
public class ReissueException extends Exception { // Make it public
    public ReissueException(String message) {
        super(message);
    }
}
