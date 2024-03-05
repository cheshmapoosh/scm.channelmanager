package ir.daneshrefah.scm.cache.client.distribution.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedJob;
import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class DistributedLockImpl<T> implements DistributedLock<T> {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public T synchronizedException(String lockGroup, Duration ttl, DistributedJob<T> distributedJob) {
        IMap<String,Object> lockMap = getLockMap();
        if (!lockMap.isLocked(lockGroup)) {
            try {
                lockMap.lock(lockGroup, ttl.getSeconds(), TimeUnit.SECONDS);
                return distributedJob.accepted();
            } finally {
                lockMap.unlock(lockGroup);
            }
        } else {
            return distributedJob.rejected();
        }
    }

    @Override
    public T synchronizedException(String lockGroup, DistributedJob<T> distributedJob) {
        IMap<String,Object> lockMap = getLockMap();
        if (!lockMap.isLocked(lockGroup)) {
            try {
                lockMap.lock(lockGroup);
                return distributedJob.accepted();
            } finally {
                lockMap.unlock(lockGroup);
            }
        } else {
            return distributedJob.rejected();
        }
    }

    private IMap<String,Object> getLockMap(){
         return hazelcastInstance.getMap("distributed-lock-config-map");
    }

}
