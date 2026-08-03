package ir.daneshrefah.scm.cache.starter.utility.resourcelease;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class HazelcastResourceLeaseUtility implements ResourceLeaseUtility {

    public static final String DEFAULT_MAP_NAME = "scm-resource-leases";

    private static final long MIN_TTL_MS = 1_000L;
    private static final long DEFAULT_TTL_MS = 30_000L;

    private final IMap<String, String> leaseMap;
    private final String ownerId;

    public HazelcastResourceLeaseUtility(HazelcastInstance hazelcastInstance) {
        this(hazelcastInstance.getMap(DEFAULT_MAP_NAME), resolveOwnerId());
    }

    HazelcastResourceLeaseUtility(IMap<String, String> leaseMap, String ownerId) {
        this.leaseMap = leaseMap;
        this.ownerId = ownerId;
    }

    @Override
    public ResourceLease acquire(String poolName, Collection<String> candidates, Duration ttl) {
        String normalizedPoolName = normalizePoolName(poolName);
        List<String> normalizedCandidates = normalizeCandidates(candidates);
        long ttlMs = normalizeTtl(ttl);

        for (String candidate : normalizedCandidates) {
            String leaseKey = leaseKey(normalizedPoolName, candidate);
            String leaseOwnerId = ownerId + ":" + UUID.randomUUID();
            try {
                String existingOwner = leaseMap.putIfAbsent(
                        leaseKey, leaseOwnerId, ttlMs, TimeUnit.MILLISECONDS);
                if (existingOwner == null) {
                    log.info("Acquired remote resource lease: pool='{}', resource='{}', owner='{}'",
                            normalizedPoolName, candidate, ownerId);
                    return new HazelcastLease(
                            normalizedPoolName, candidate, leaseKey, leaseOwnerId, ttlMs);
                }
            } catch (Exception exception) {
                throw new ResourceLeaseAcquireException(
                        "Could not acquire remote resource lease for pool='" + normalizedPoolName + "', resource='" + candidate + "'",
                        exception);
            }
        }

        throw new ResourceLeaseAcquireException(
                "No available remote resource in pool='" + normalizedPoolName + "' for candidates=" + normalizedCandidates);
    }

    private String normalizePoolName(String poolName) {
        if (!StringUtils.hasText(poolName)) {
            throw new IllegalArgumentException("poolName must not be blank");
        }
        return poolName.trim();
    }

    private List<String> normalizeCandidates(Collection<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        List<String> normalized = new ArrayList<>(candidates.size());
        for (String candidate : candidates) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            normalized.add(candidate.trim());
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("candidates must contain at least one non-blank resource");
        }
        return normalized;
    }

    private long normalizeTtl(Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            return DEFAULT_TTL_MS;
        }
        return Math.max(MIN_TTL_MS, ttl.toMillis());
    }

    private String leaseKey(String poolName, String candidate) {
        return poolName + "::" + candidate;
    }

    private static String resolveOwnerId() {
        String hostName = System.getenv().getOrDefault("HOSTNAME", "unknown-host");
        return hostName + ":" + UUID.randomUUID();
    }

    private final class HazelcastLease implements ResourceLease {
        private final String poolName;
        private final String resourceName;
        private final String leaseKey;
        private final String leaseOwnerId;
        private final long ttlMs;
        private final ScheduledExecutorService refresher;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final AtomicBoolean valid = new AtomicBoolean(true);
        private final List<Runnable> invalidationListeners = new CopyOnWriteArrayList<>();

        private HazelcastLease(
                String poolName,
                String resourceName,
                String leaseKey,
                String leaseOwnerId,
                long ttlMs
        ) {
            this.poolName = poolName;
            this.resourceName = resourceName;
            this.leaseKey = leaseKey;
            this.leaseOwnerId = leaseOwnerId;
            this.ttlMs = ttlMs;
            this.refresher = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "remote-resource-lease-" + resourceName);
                thread.setDaemon(true);
                return thread;
            });

            long refreshEveryMs = Math.max(MIN_TTL_MS, ttlMs / 3L);
            refresher.scheduleAtFixedRate(this::refresh, refreshEveryMs, refreshEveryMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public String poolName() {
            return poolName;
        }

        @Override
        public String resourceName() {
            return resourceName;
        }

        @Override
        public boolean isValid() {
            return valid.get() && !closed.get();
        }

        @Override
        public void onInvalidated(Runnable listener) {
            if (listener == null) {
                return;
            }
            invalidationListeners.add(listener);
            if (!isValid()) {
                notifyListener(listener);
            }
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            valid.set(false);
            refresher.shutdownNow();
            try {
                boolean released = leaseMap.remove(leaseKey, leaseOwnerId);
                if (released) {
                    log.info("Released remote resource lease: pool='{}', resource='{}', owner='{}'",
                            poolName, resourceName, ownerId);
                } else {
                    log.warn("Remote resource lease was not released because ownership was absent: "
                                    + "pool='{}', resource='{}', owner='{}'",
                            poolName, resourceName, ownerId);
                }
            } catch (Exception exception) {
                log.warn("Could not release remote resource lease: pool='{}', resource='{}', owner='{}'",
                        poolName, resourceName, ownerId, exception);
            }
        }

        private void refresh() {
            try {
                boolean renewed = leaseMap.replace(leaseKey, leaseOwnerId, leaseOwnerId);
                if (renewed) {
                    return;
                }
                log.warn("Remote resource lease was lost before refresh: pool='{}', resource='{}', owner='{}'",
                        poolName, resourceName, ownerId);
                invalidate();
            } catch (Exception exception) {
                log.warn("Could not refresh remote resource lease: pool='{}', resource='{}', owner='{}'",
                        poolName, resourceName, ownerId, exception);
                invalidate();
            }
        }

        private void invalidate() {
            if (!valid.compareAndSet(true, false)) {
                return;
            }
            refresher.shutdownNow();
            invalidationListeners.forEach(this::notifyListener);
        }

        private void notifyListener(Runnable listener) {
            try {
                listener.run();
            } catch (RuntimeException exception) {
                log.warn("Resource lease invalidation listener failed: pool='{}', resource='{}'",
                        poolName, resourceName, exception);
            }
        }
    }
}
