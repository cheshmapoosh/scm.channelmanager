package ir.daneshrefah.scm.cache.client.distribution.service;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.cache.client.distribution.exception.LockTimeOutException;
import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedJob;
import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


@Component
@RequiredArgsConstructor
public class DistributedLockImpl<T> implements DistributedLock<T> {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public T distributedJob(String lockName, Duration ttl, DistributedJob<T> acceptedJob, DistributedJob<T> rejectedJob) {
        IMap<String, Object> lockMap = getLockMap();
        if (!lockMap.isLocked(lockName)) {
            try {
                lockMap.lock(lockName, ttl.getSeconds(), TimeUnit.SECONDS);
                return acceptedJob.execute();
            } finally {
                lockMap.unlock(lockName);
            }
        } else {
            if (Objects.nonNull(rejectedJob)) {
                return rejectedJob.execute();
            }
            return null;
        }
    }


    @Override
    public T distributedJob(String lockName, DistributedJob<T> acceptedJob, DistributedJob<T> rejectedJob) {
        IMap<String, Object> lockMap = getLockMap();
        if (!lockMap.isLocked(lockName)) {
            try {
                lockMap.lock(lockName);
                return acceptedJob.execute();
            } finally {
                lockMap.unlock(lockName);
            }
        } else {
            if (Objects.nonNull(rejectedJob)) {
                return rejectedJob.execute();
            }
            return null;
        }
    }

    @Override
    public T synchronizedJob(String lockName, int ttl, TimeUnit timeUnit, DistributedJob<T> Job) throws InterruptedException, LockTimeOutException {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        Lock lock = hazelcastInstance.getCPSubsystem().getLock(lockName);
        if (lock.tryLock(ttl, timeUnit)) {
            try {
                return Job.execute();
            } finally {
                lock.unlock();
            }
        } else {
            throw new LockTimeOutException();
        }
    }

    private IMap<String, Object> getLockMap() {
        return hazelcastInstance.getMap("distributed-lock-config-map");
    }

}
