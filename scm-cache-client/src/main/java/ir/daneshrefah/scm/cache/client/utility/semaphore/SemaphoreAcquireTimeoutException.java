package ir.daneshrefah.scm.cache.client.utility.semaphore;

import java.time.Duration;

public class SemaphoreAcquireTimeoutException extends RuntimeException {

    public SemaphoreAcquireTimeoutException(String semaphoreName, Duration waitTime) {
        super("Could not acquire semaphore '" + semaphoreName + "' within waitTime=" + waitTime);
    }
}
