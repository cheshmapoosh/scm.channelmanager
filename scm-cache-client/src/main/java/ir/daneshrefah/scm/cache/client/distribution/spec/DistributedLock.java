package ir.daneshrefah.scm.cache.client.distribution.spec;

import java.time.Duration;

public interface DistributedLock<T> {
    T syncroziedExceution(String lockGroup, Duration ttl, DistributedJob<T> distributedJob);

    T syncroziedExceution(String lockGroup, DistributedJob<T> distributedJob);
}
