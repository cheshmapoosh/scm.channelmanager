package ir.daneshrefah.scm.cache.starter.connector.routing;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;

import java.time.Duration;

public record CacheRoute(
        String cacheName,
        String targetName,
        CacheType type,
        Duration ttl,
        long maximumSize,
        TtlOwnership ttlOwnership,
        SizeOwnership sizeOwnership
) {

    public CacheRoute(String cacheName, String targetName, CacheType type, Duration ttl, long maximumSize) {
        this(cacheName, targetName, type, ttl, maximumSize, TtlOwnership.HOST, SizeOwnership.LOCAL);
    }

    public Duration defaultOperationTtl() {
        return ttlOwnership == TtlOwnership.HOST ? ttl : null;
    }

    public boolean serverManagedTtl() {
        return ttlOwnership == TtlOwnership.SERVER_MANAGED;
    }

    public enum TtlOwnership {
        HOST,
        SERVER_MANAGED
    }

    public enum SizeOwnership {
        LOCAL,
        NEAR_CLIENT,
        NOT_APPLICABLE
    }
}
