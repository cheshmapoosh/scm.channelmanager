package ir.daneshrefah.scm.cache.client.distribution.spec;

import java.time.Duration;

public interface DistributedLock<T> {
    T synchronizedException(String lockGroup, Duration ttl, DistributedJob<T> distributedJob);

    T synchronizedException(String lockGroup, DistributedJob<T> distributedJob);
}
