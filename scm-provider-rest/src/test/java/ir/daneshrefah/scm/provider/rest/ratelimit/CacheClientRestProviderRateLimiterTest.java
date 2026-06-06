package ir.daneshrefah.scm.provider.rest.ratelimit;

import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheClientRestProviderRateLimiterTest {

    @Test
    void usesOperationKeyWhenConfigured() {
        FakeRateLimiterUtility utility = new FakeRateLimiterUtility(
                new RateLimitResult("bucket-a", "ignored", 1, true, true, 10, 0, 0)
        );
        CacheClientRestProviderRateLimiter limiter = new CacheClientRestProviderRateLimiter(utility, new RestProviderMetrics());

        limiter.acquire(config("poba-hps", true, "bucket-a", "operation"), "cardInquiry");

        assertEquals("bucket-a", utility.bucketName);
        assertEquals("cardInquiry", utility.key);
    }

    @Test
    void incrementsRateLimitedMetricAndThrowsWhenRejected() {
        FakeRateLimiterUtility utility = new FakeRateLimiterUtility(
                new RateLimitResult("bucket-b", "ignored", 1, false, true, 0, 1_000_000_000L, 0)
        );
        RestProviderMetrics metrics = new RestProviderMetrics();
        CacheClientRestProviderRateLimiter limiter = new CacheClientRestProviderRateLimiter(utility, metrics);

        assertThrows(IllegalStateException.class,
                () -> limiter.acquire(config("nab-apirepo", true, "bucket-b", "provider"), "op"));
        assertEquals(1, metrics.provider("nab-apirepo").rateLimitedCount());
    }

    private RestProviderResolvedConfig config(String provider, boolean enabled, String bucket, String key) {
        return new RestProviderResolvedConfig(
                provider,
                "rest",
                "https://example.com",
                3000,
                6000,
                true,
                false,
                RestProviderResolvedConfig.HttpRedirect.NORMAL,
                "POST",
                Map.of(),
                Map.of(),
                java.util.List.of(),
                new RestProviderResolvedConfig.Proxy(null, null, null, null),
                new RestProviderResolvedConfig.Auth(
                        RestProviderResolvedConfig.AuthType.NONE,
                        "Authorization",
                        null,
                        null,
                        null,
                        null,
                        true
                ),
                new RestProviderResolvedConfig.Security(java.util.List.of(), java.util.List.of(), 400),
                new RestProviderResolvedConfig.RateLimit(enabled, bucket, key)
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
