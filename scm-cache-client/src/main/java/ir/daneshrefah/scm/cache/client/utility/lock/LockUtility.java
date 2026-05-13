package ir.daneshrefah.scm.cache.client.utility.lock;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface LockUtility {

    <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job);

    default <T> T executeWithLock(String lockName, Callable<T> job) {
        return executeWithLock(lockName, Duration.ZERO, job);
    }

    default void runWithLock(String lockName, Duration waitTime, Runnable job) {
        executeWithLock(lockName, waitTime, () -> {
            job.run();
            return null;
        });
    }

    default void runWithLock(String lockName, Runnable job) {
        runWithLock(lockName, Duration.ZERO, job);
    }
}
