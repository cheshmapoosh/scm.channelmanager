package ir.daneshrefah.scm.provider.shetab.ratelimit;

import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheClientShetabRateLimiterTest {

    @Test
    void usesOperationKeyWhenConfigured() {
        FakeRateLimiterUtility utility = new FakeRateLimiterUtility(
                new RateLimitResult("bucket-a", "ignored", 1, true, true, 10, 0, 0)
        );
        CacheClientShetabRateLimiter limiter = new CacheClientShetabRateLimiter(utility, new ShetabProviderMetrics());
        limiter.acquire(config("hps", true, "bucket-a", "operation"), "cardInquiry");

        assertEquals("bucket-a", utility.bucketName);
        assertEquals("cardInquiry", utility.key);
    }

    @Test
    void incrementsRateLimitedMetricAndThrowsWhenRejected() {
        FakeRateLimiterUtility utility = new FakeRateLimiterUtility(
                new RateLimitResult("bucket-b", "ignored", 1, false, true, 0, 1_000_000_000L, 0)
        );
        ShetabProviderMetrics metrics = new ShetabProviderMetrics();
        CacheClientShetabRateLimiter limiter = new CacheClientShetabRateLimiter(utility, metrics);

        assertThrows(IllegalStateException.class,
                () -> limiter.acquire(config("hps", true, "bucket-b", "provider"), "op"));
        assertEquals(1, metrics.provider("hps").rateLimitedCount());
    }

    private ShetabResolvedConfig config(String provider, boolean enabled, String bucket, String key) {
        return new ShetabResolvedConfig(
                provider,
                List.of(),
                null,
                null,
                3000,
                1000,
                6000,
                1000,
                1000,
                3,
                1000,
                new ShetabResolvedConfig.RateLimit(enabled, bucket, key),
                new ShetabResolvedConfig.EndpointLease(false, 30000)
        );
    }

    private static final class FakeRateLimiterUtility implements RateLimiterUtility {
        private final RateLimitResult result;
        private String bucketName;
        private String key;

        private FakeRateLimiterUtility(RateLimitResult result) {
            this.result = result;
        }

        @Override
        public RateLimitResult tryConsume(String bucketName, String key) {
            this.bucketName = bucketName;
            this.key = key;
            return result;
        }

        @Override
        public RateLimitResult tryConsume(String bucketName, String key, int tokenCountUsage) {
            this.bucketName = bucketName;
            this.key = key;
            return result;
        }
    }
}
