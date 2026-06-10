package ir.daneshrefah.scm.cache.client.utility.lock;

import java.time.Duration;

public class LockAcquireFailedException extends RuntimeException {

    public LockAcquireFailedException(String lockName, Duration waitTime) {
        super("Could not acquire lock '" + lockName + "' in waitTime=" + waitTime);
    }
}
