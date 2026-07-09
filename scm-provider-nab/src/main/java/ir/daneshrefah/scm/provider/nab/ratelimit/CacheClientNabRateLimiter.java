package ir.daneshrefah.scm.provider.nab.ratelimit;

import ir.daneshrefah.scm.cache.starter.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.metrics.NabProviderMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class CacheClientNabRateLimiter implements NabRateLimiter {
    private final RateLimiterUtility rateLimiterUtility;
    private final NabProviderMetrics metrics;

    @Override
    public void acquire(NabResolvedConfig config, String operationName) {
        NabResolvedConfig.RateLimit rateLimit = config.rateLimit();
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
            log.warn("NAB rate limit exceeded provider={} bucket={} key={} retryAfterSeconds={}",
                    config.provider(), bucket, key, result.retryAfterSeconds());
            throw new IllegalStateException("NAB rate limit exceeded provider=" + config.provider()
                    + ", bucket=" + bucket
                    + ", key=" + key
                    + ", retryAfterSeconds=" + result.retryAfterSeconds());
        }

        if (!result.bucketConfigured()) {
            log.warn("NAB rate limit bucket is not configured provider={} bucket={} key={} (allowed by policy)",
                    config.provider(), bucket, key);
        }
    }

    private String normalizeBucket(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            return "nab-default";
        }
        return bucket.trim();
    }

    private String resolveKey(NabResolvedConfig config, String operationName, String configuredKey) {
        String key = configuredKey == null || configuredKey.isBlank() ? "provider" : configuredKey.trim();
        String normalizedOperation = operationName == null || operationName.isBlank() ? "default" : operationName.trim();
        return switch (key) {
            case "operation" -> normalizedOperation;
            case "provider-operation" -> config.provider() + ":" + normalizedOperation;
            default -> config.provider();
        };
    }
}
