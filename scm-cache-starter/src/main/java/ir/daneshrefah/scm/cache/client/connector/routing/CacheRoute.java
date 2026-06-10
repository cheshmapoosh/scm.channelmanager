package ir.daneshrefah.scm.cache.client.connector.routing;

import ir.daneshrefah.scm.cache.client.config.properties.CacheType;

import java.time.Duration;

public record CacheRoute(
        String cacheName,
        String targetName,
        CacheType type,
        Duration ttl,
        long maximumSize
) {
}
