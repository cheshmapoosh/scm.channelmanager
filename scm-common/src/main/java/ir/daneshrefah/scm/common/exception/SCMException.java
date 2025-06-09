package ir.daneshrefah.scm.common.exception;

public class SCMException extends RuntimeException {

    public SCMException() {
        super();
    }

    public SCMException(String message) {
        super(message);
    }

    public SCMException(String message, Throwable cause) {
        super(message, cause);
    }

    public SCMException(Throwable cause) {
        super(cause);
    }

    protected SCMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
