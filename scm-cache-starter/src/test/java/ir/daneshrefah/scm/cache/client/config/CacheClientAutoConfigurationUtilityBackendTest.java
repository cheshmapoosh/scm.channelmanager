package ir.daneshrefah.scm.cache.client.config;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.config.properties.RateLimitProperties;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.RoutingConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.RoutingLockUtility;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.RoutingRateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.RoutingResourceLeaseUtility;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheClientAutoConfigurationUtilityBackendTest {

    private final CacheClientAutoConfiguration configuration = new CacheClientAutoConfiguration();

    @Test
    void createsLocalUtilityBeansWhenConfigured() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.getUtilities().setRateLimit(CacheClientProperties.UtilityBackendType.LOCAL);
        properties.getUtilities().setLock(CacheClientProperties.UtilityBackendType.LOCAL);
        properties.getUtilities().setConcurrencyLimit(CacheClientProperties.UtilityBackendType.LOCAL);
        properties.getUtilities().setResourceLease(CacheClientProperties.UtilityBackendType.LOCAL);

        assertInstanceOf(
                RoutingRateLimitBucketService.class,
                configuration.rateLimitBucketService(Optional.empty(), new RateLimitProperties(), properties)
        );
        assertInstanceOf(RoutingLockUtility.class, configuration.lockUtility(Optional.empty(), properties));
        assertInstanceOf(RoutingConcurrencyLimiterUtility.class, configuration.concurrencyLimiterUtility(Optional.empty(), properties));
        assertInstanceOf(RoutingResourceLeaseUtility.class, configuration.resourceLeaseUtility(Optional.empty(), properties));
    }

    @Test
    void failsFastWhenRemoteUtilityBackendHasNoHazelcastInstance() {
        CacheClientProperties properties = new CacheClientProperties();

        assertThrows(
                IllegalStateException.class,
                () -> configuration.rateLimitBucketService(Optional.empty(), new RateLimitProperties(), properties)
        );
        assertThrows(IllegalStateException.class, () -> configuration.lockUtility(Optional.empty(), properties));
        assertThrows(IllegalStateException.class, () -> configuration.concurrencyLimiterUtility(Optional.empty(), properties));
        assertThrows(IllegalStateException.class, () -> configuration.resourceLeaseUtility(Optional.empty(), properties));
    }
}
