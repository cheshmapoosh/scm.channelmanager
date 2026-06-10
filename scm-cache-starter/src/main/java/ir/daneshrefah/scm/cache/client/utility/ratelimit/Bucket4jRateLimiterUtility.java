package ir.daneshrefah.scm.cache.client.utility.ratelimit;

import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.EstimationProbe;
import ir.daneshrefah.scm.cache.client.config.properties.RateLimitProperties;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.RateLimitBucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class Bucket4jRateLimiterUtility implements RateLimiterUtility {

    private final RateLimitBucketService rateLimitBucketService;
    private final RateLimitProperties rateLimitProperties;

    @Override
    public RateLimitResult tryConsume(String bucketName, String key) {
        return tryConsume(bucketName, key, 1);
    }

    @Override
    public RateLimitResult tryConsume(String bucketName, String key, int tokenCountUsage) {
        int requestedTokens = tokenCountUsage > 0 ? tokenCountUsage : 1;
        Optional<Bucket> bucketOptional = rateLimitBucketService.resolveBucket(bucketName, key);
        if (bucketOptional.isEmpty()) {
            return missingBucketResult(bucketName, key, requestedTokens);
        }

        Bucket bucket = bucketOptional.get();
        ConsumptionProbe firstProbe = bucket.tryConsumeAndReturnRemaining(requestedTokens);
        RateLimitResult firstResult = fromProbe(bucketName, key, requestedTokens, firstProbe);
        if (firstResult.allowed()) {
            return firstResult;
        }
        if (rateLimitProperties.getOverflowPolicy() != RateLimitProperties.OverflowPolicy.WAIT) {
            return firstResult;
        }

        long maxWaitNanos = toNanosSafely(resolveMaxWaitDuration(bucketName));
        if (maxWaitNanos <= 0) {
            return firstResult;
        }

        try {
            log.debug("Rate limit wait started: bucket='{}', key='{}', waitNanos={}", bucketName, key, maxWaitNanos);
            boolean consumed = bucket.asBlocking().tryConsume(requestedTokens, maxWaitNanos, BlockingStrategy.PARKING);
            if (consumed) {
                long remaining = bucket.getAvailableTokens();
                log.debug("Rate limit wait completed: bucket='{}', key='{}', remaining={}", bucketName, key, remaining);
                return new RateLimitResult(
                        bucketName,
                        key,
                        requestedTokens,
                        true,
                        true,
                        remaining,
                        0,
                        0
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Rate limit wait interrupted for bucket='{}', key='{}'", bucketName, key);
        }

        EstimationProbe estimation = bucket.estimateAbilityToConsume(requestedTokens);
        return new RateLimitResult(
                bucketName,
                key,
                requestedTokens,
                false,
                true,
                estimation.getRemainingTokens(),
                estimation.getNanosToWaitForRefill(),
                estimation.getNanosToWaitForRefill()
        );
    }

    private RateLimitResult fromProbe(String bucketName, String key, int requestedTokens, ConsumptionProbe probe) {
        return new RateLimitResult(
                bucketName,
                key,
                requestedTokens,
                probe.isConsumed(),
                true,
                probe.getRemainingTokens(),
                probe.getNanosToWaitForRefill(),
                probe.getNanosToWaitForReset()
        );
    }

    private RateLimitResult missingBucketResult(String bucketName, String key, int requestedTokens) {
        boolean allow = rateLimitProperties.getMissingBucketPolicy() == RateLimitProperties.MissingBucketPolicy.ALLOW;
        if (allow) {
            log.warn("Rate-limit bucket '{}' is not configured. request is allowed by policy.", bucketName);
        } else {
            log.warn("Rate-limit bucket '{}' is not configured. request is rejected by policy.", bucketName);
        }
        return RateLimitResult.missingBucket(bucketName, key, requestedTokens, allow);
    }

    private long toNanosSafely(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return 0;
        }
        return TimeUnit.NANOSECONDS.convert(duration);
    }

    private Duration resolveMaxWaitDuration(String bucketName) {
        Map<String, RateLimitProperties.RateLimitDefinition> definitions = rateLimitProperties.getDefinitions();
        if (definitions != null) {
            RateLimitProperties.RateLimitDefinition definition = definitions.get(bucketName);
            if (definition != null && definition.getMaxWaitDuration() != null) {
                return definition.getMaxWaitDuration();
            }
        }
        return rateLimitProperties.getMaxWaitDuration();
    }
}
