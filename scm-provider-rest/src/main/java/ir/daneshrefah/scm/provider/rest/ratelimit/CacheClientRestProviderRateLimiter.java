package ir.daneshrefah.scm.provider.rest.ratelimit;

import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class CacheClientRestProviderRateLimiter implements RestProviderRateLimiter {
    private final RateLimiterUtility rateLimiterUtility;
    private final RestProviderMetrics metrics;

    @Override
    public void acquire(RestProviderResolvedConfig config, String operationName) {
        RestProviderResolvedConfig.RateLimit rateLimit = config.rateLimit();
        if (rateLimit == null || !rateLimit.enabled()) {
            return;
        }

        String bucket = normalizeBucket(rateLimit.bucket());
        String key = resolveKey(config, operationName, rateLimit.key());

        long startedAt = System.nanoTime();
        RateLimitResult result = rateLimiterUtility.tryConsume(bucket, key);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        if (elapsedMs > 0) {
            metrics.provider(config.provider()).rateLimitWait();
        }

        if (!result.allowed()) {
            metrics.provider(config.provider()).rateLimited();
            log.warn("REST rate limit exceeded provider={} bucket={} key={} retryAfterSeconds={}",
                    config.provider(), bucket, key, result.retryAfterSeconds());
            throw new IllegalStateException("REST rate limit exceeded provider=" + config.provider()
                    + ", bucket=" + bucket
                    + ", key=" + key
                    + ", retryAfterSeconds=" + result.retryAfterSeconds());
        }

        if (!result.bucketConfigured()) {
            log.warn("REST rate limit bucket is not configured provider={} bucket={} key={} (allowed by policy)",
                    config.provider(), bucket, key);
        }
    }

    private String normalizeBucket(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            return "rest-default";
        }
        return bucket.trim();
    }

    private String resolveKey(RestProviderResolvedConfig config, String operationName, String configuredKey) {
        String key = configuredKey == null || configuredKey.isBlank() ? "provider" : configuredKey.trim();
        String normalizedOperation = operationName == null || operationName.isBlank() ? "default" : operationName.trim();
        return switch (key) {
            case "operation" -> normalizedOperation;
            case "provider-operation" -> config.provider() + ":" + normalizedOperation;
            default -> config.provider();
        };
    }
}
