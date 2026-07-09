package ir.daneshrefah.scm.cache.starter.utility.concurrencylimit;

import java.time.Duration;

public class ConcurrencyLimitAcquireTimeoutException extends RuntimeException {

    public ConcurrencyLimitAcquireTimeoutException(String limitName, Duration waitTime) {
        super("Could not acquire concurrency limit '" + limitName + "' within waitTime=" + waitTime);
    }
}
