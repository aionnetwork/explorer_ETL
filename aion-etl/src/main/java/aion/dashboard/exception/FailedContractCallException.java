package aion.dashboard.exception;

public class FailedContractCallException extends RuntimeException {

    public FailedContractCallException() {
    }

    public FailedContractCallException(String message) {
        super(message);
    }

    public FailedContractCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedContractCallException(Throwable cause) {
        super(cause);
    }
}
