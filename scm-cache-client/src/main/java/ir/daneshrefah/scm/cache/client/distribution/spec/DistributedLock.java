package ir.daneshrefah.scm.cache.client.distribution.spec;

import ir.daneshrefah.scm.cache.client.distribution.exception.LockTimeOutException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface DistributedLock<T> {
    T distributedJob(String lockName, Duration ttl, DistributedJob<T> acceptedJob, DistributedJob<T> rejectedJob);

    T distributedJob(String lockName, DistributedJob<T> acceptedJob, DistributedJob<T> rejectedJob);

    T synchronizedJob(String lockName, int ttl, TimeUnit timeUnit, DistributedJob<T> Job) throws InterruptedException, LockTimeOutException;
}
