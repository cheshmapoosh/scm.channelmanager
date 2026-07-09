package ir.daneshrefah.scm.cache.starter.utility.resourcelease;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ResourceLeaseUtility {

    ResourceLease acquire(String poolName, Collection<String> candidates, Duration ttl);

    default ResourceLease acquire(String poolName, Collection<String> candidates) {
        return acquire(poolName, candidates, Duration.ofSeconds(30));
    }

    default <T> T executeWithLease(
            String poolName,
            Collection<String> candidates,
            Duration ttl,
            Function<ResourceLease, T> job
    ) {
        Objects.requireNonNull(job, "job must not be null");
        ResourceLease lease = acquire(poolName, candidates, ttl);
        try {
            return job.apply(lease);
        } finally {
            lease.close();
        }
    }

    default void runWithLease(
            String poolName,
            Collection<String> candidates,
            Duration ttl,
            Consumer<ResourceLease> job
    ) {
        executeWithLease(poolName, candidates, ttl, lease -> {
            job.accept(lease);
            return null;
        });
    }

    default <T> T executeWithLease(
            String poolName,
            Collection<String> candidates,
            Duration ttl,
            Function<ResourceLease, T> job,
            Function<RuntimeException, T> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            return executeWithLease(poolName, candidates, ttl, job);
        } catch (RuntimeException exception) {
            return onFailure.apply(exception);
        }
    }

    default void runWithLease(
            String poolName,
            Collection<String> candidates,
            Duration ttl,
            Consumer<ResourceLease> job,
            Consumer<RuntimeException> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            runWithLease(poolName, candidates, ttl, job);
        } catch (RuntimeException exception) {
            onFailure.accept(exception);
        }
    }
}
