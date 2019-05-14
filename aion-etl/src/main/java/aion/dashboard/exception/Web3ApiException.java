package aion.dashboard.exception;

public class Web3ApiException extends Exception {

    public Web3ApiException() {
        super();
    }

    public Web3ApiException(String message) {
        super(message);
    }

    public Web3ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public Web3ApiException(Throwable cause) {
        super(cause);
    }
}
