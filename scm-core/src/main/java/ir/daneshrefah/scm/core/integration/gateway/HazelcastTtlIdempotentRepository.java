package ir.daneshrefah.scm.core.integration.gateway;

import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.support.service.ServiceSupport;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

public class HazelcastTtlIdempotentRepository extends ServiceSupport
        implements IdempotentRepository {

    private static final String PRESENT = "1";

    private final Cache cache;

    public HazelcastTtlIdempotentRepository(
            CacheManager cacheManager,
            String cacheName) {

        Objects.requireNonNull(cacheManager, "cacheManager");
        Objects.requireNonNull(cacheName, "cacheName");

        this.cache = cacheManager.getCache(cacheName);

        if (this.cache == null) {
            throw new IllegalStateException(
                    "Cache '" + cacheName + "' not found in CacheManager"
            );
        }
    }

    @Override
    public boolean add(String key) {
        Objects.requireNonNull(key, "key");

        return cache.putIfAbsent(key, PRESENT) == null;
    }

    @Override
    public boolean contains(String key) {
        Objects.requireNonNull(key, "key");

        return cache.get(key) != null;
    }

    @Override
    public boolean remove(String key) {
        Objects.requireNonNull(key, "key");

        boolean exists = cache.get(key) != null;

        if (exists) {
            cache.evict(key);
        }

        return exists;
    }

    @Override
    public boolean confirm(String key) {
        return true;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    protected void doStart() {
        // No resources to initialize.
    }

    @Override
    protected void doStop() {
        // Cache lifecycle is managed by Spring.
    }
}