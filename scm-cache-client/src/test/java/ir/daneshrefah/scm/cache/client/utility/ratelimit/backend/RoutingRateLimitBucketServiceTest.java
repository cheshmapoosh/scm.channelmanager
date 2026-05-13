package ir.daneshrefah.scm.cache.client.utility.ratelimit.backend;

import io.github.bucket4j.Bucket;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class RoutingRateLimitBucketServiceTest {

    @Test
    void routesBucketByNameOverride() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setRateLimit(CacheClientProperties.UtilityBackendType.REMOTE);
        utilities.getRateLimitNames().put("login", CacheClientProperties.UtilityBackendType.LOCAL);
        Bucket localBucket = mock(Bucket.class);
        Bucket remoteBucket = mock(Bucket.class);

        RoutingRateLimitBucketService service = new RoutingRateLimitBucketService(
                (bucketName, key) -> Optional.of(localBucket),
                (bucketName, key) -> Optional.of(remoteBucket),
                utilities
        );

        assertSame(localBucket, service.resolveBucket("login", "uid::1").orElseThrow());
        assertSame(remoteBucket, service.resolveBucket("otp", "uid::1").orElseThrow());
    }
}
