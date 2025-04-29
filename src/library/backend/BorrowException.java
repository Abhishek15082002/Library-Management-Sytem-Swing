package library.backend;

/**
 * Custom exception for borrow-related business rule failures.
 */
public class BorrowException extends Exception { // Make it public
    public BorrowException(String message) {
        super(message);
    }
}
