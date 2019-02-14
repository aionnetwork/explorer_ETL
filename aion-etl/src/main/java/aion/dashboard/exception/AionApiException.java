package aion.dashboard.exception;

/**
 * Thrown in class of an error message from the API.
 */
public class AionApiException extends Exception {
    public AionApiException(String message) {
        super(message);
    }

    public AionApiException(){
        this("Aion threw an exception when retrieving message");
    }
}
