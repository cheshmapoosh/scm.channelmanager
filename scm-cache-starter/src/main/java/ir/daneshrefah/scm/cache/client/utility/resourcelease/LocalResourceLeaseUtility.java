package ir.daneshrefah.scm.cache.client.utility.resourcelease;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LocalResourceLeaseUtility implements ResourceLeaseUtility {

    private static final long MIN_TTL_MS = 1_000L;
    private static final long DEFAULT_TTL_MS = 30_000L;

    private final Map<String, LeaseState> leaseStates = new ConcurrentHashMap<>();
    private final String ownerId = "local-" + UUID.randomUUID();

    @Override
    public ResourceLease acquire(String poolName, Collection<String> candidates, Duration ttl) {
        String normalizedPoolName = normalizePoolName(poolName);
        List<String> normalizedCandidates = normalizeCandidates(candidates);
        long ttlMs = normalizeTtl(ttl);

        for (String candidate : normalizedCandidates) {
            String leaseKey = leaseKey(normalizedPoolName, candidate);
            if (tryAcquire(leaseKey, ttlMs)) {
                log.info("Acquired local resource lease: pool='{}', resource='{}', owner='{}'",
                        normalizedPoolName, candidate, ownerId);
                return new LocalLease(normalizedPoolName, candidate, leaseKey, ttlMs);
            }
        }

        throw new ResourceLeaseAcquireException(
                "No available local resource in pool='" + normalizedPoolName + "' for candidates=" + normalizedCandidates);
    }

    private boolean tryAcquire(String leaseKey, long ttlMs) {
        synchronized (leaseStates) {
            long now = System.currentTimeMillis();
            LeaseState current = leaseStates.get(leaseKey);
            if (current == null || current.expiresAtMs <= now) {
                leaseStates.put(leaseKey, new LeaseState(ownerId, now + ttlMs));
                return true;
            }
            return false;
        }
    }

    private boolean refreshIfOwned(String leaseKey, long ttlMs) {
        synchronized (leaseStates) {
            LeaseState current = leaseStates.get(leaseKey);
            if (current == null || !ownerId.equals(current.ownerId)) {
                return false;
            }
            current.expiresAtMs = System.currentTimeMillis() + ttlMs;
            return true;
        }
    }

    private void releaseIfOwned(String leaseKey) {
        synchronized (leaseStates) {
            LeaseState current = leaseStates.get(leaseKey);
            if (current != null && ownerId.equals(current.ownerId)) {
                leaseStates.remove(leaseKey);
            }
        }
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

    private static final class LeaseState {
        private final String ownerId;
        private long expiresAtMs;

        private LeaseState(String ownerId, long expiresAtMs) {
            this.ownerId = ownerId;
            this.expiresAtMs = expiresAtMs;
        }
    }

    private final class LocalLease implements ResourceLease {
        private final String poolName;
        private final String resourceName;
        private final String leaseKey;
        private final long ttlMs;
        private final ScheduledExecutorService refresher;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private LocalLease(String poolName, String resourceName, String leaseKey, long ttlMs) {
            this.poolName = poolName;
            this.resourceName = resourceName;
            this.leaseKey = leaseKey;
            this.ttlMs = ttlMs;
            this.refresher = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "local-resource-lease-" + resourceName);
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
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            refresher.shutdownNow();
            releaseIfOwned(leaseKey);
            log.info("Released local resource lease: pool='{}', resource='{}', owner='{}'",
                    poolName, resourceName, ownerId);
        }

        private void refresh() {
            try {
                boolean refreshed = refreshIfOwned(leaseKey, ttlMs);
                if (!refreshed) {
                    log.warn("Local resource lease was lost before refresh: pool='{}', resource='{}', owner='{}'",
                            poolName, resourceName, ownerId);
                    close();
                }
            } catch (Exception exception) {
                log.warn("Could not refresh local resource lease: pool='{}', resource='{}', owner='{}'",
                        poolName, resourceName, ownerId, exception);
            }
        }
    }
}
