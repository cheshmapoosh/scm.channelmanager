package ir.daneshrefah.scm.cache.starter.utility.concurrencylimit;

public class ConcurrencyLimitOperationException extends RuntimeException {

    public ConcurrencyLimitOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
