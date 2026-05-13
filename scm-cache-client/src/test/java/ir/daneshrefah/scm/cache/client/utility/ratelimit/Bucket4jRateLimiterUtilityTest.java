package ir.daneshrefah.scm.cache.client.utility.ratelimit;

import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import ir.daneshrefah.scm.cache.client.config.properties.RateLimitProperties;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.RateLimitBucketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Bucket4jRateLimiterUtilityTest {

    @Mock
    private RateLimitBucketService rateLimitBucketService;
    @Mock
    private Bucket bucket;
    @Mock
    private ConsumptionProbe firstProbe;
    @Mock
    private BlockingBucket blockingBucket;

    @Test
    void usesBucketSpecificMaxQueueWaitDurationWhenConfigured() throws Exception {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setOverflowPolicy(RateLimitProperties.OverflowPolicy.WAIT);
        properties.setMaxWaitDuration(Duration.ofSeconds(3));
        properties.setDefinitions(Map.of(
                "uaa_login", definitionWithQueueWait(Duration.ofSeconds(8))
        ));

        when(rateLimitBucketService.resolveBucket("uaa_login", "k1")).thenReturn(Optional.of(bucket));
        when(bucket.tryConsumeAndReturnRemaining(2)).thenReturn(firstProbe);
        when(firstProbe.isConsumed()).thenReturn(false);
        when(firstProbe.getRemainingTokens()).thenReturn(0L);
        when(firstProbe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L);
        when(firstProbe.getNanosToWaitForReset()).thenReturn(1_000_000_000L);
        when(bucket.asBlocking()).thenReturn(blockingBucket);
        when(blockingBucket.tryConsume(2, Duration.ofSeconds(8).toNanos(), BlockingStrategy.PARKING)).thenReturn(true);
        when(bucket.getAvailableTokens()).thenReturn(4L);

        Bucket4jRateLimiterUtility utility = new Bucket4jRateLimiterUtility(rateLimitBucketService, properties);

        RateLimitResult result = utility.tryConsume("uaa_login", "k1", 2);

        assertTrue(result.allowed());
        assertTrue(result.bucketConfigured());
        verify(blockingBucket).tryConsume(2, Duration.ofSeconds(8).toNanos(), BlockingStrategy.PARKING);
    }

    @Test
    void doesNotBlockWhenOverflowPolicyIsReject() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setOverflowPolicy(RateLimitProperties.OverflowPolicy.REJECT);

        when(rateLimitBucketService.resolveBucket("uaa_login", "k1")).thenReturn(Optional.of(bucket));
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(firstProbe);
        when(firstProbe.isConsumed()).thenReturn(false);
        when(firstProbe.getRemainingTokens()).thenReturn(0L);
        when(firstProbe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L);
        when(firstProbe.getNanosToWaitForReset()).thenReturn(1_000_000_000L);

        Bucket4jRateLimiterUtility utility = new Bucket4jRateLimiterUtility(rateLimitBucketService, properties);

        RateLimitResult result = utility.tryConsume("uaa_login", "k1");

        assertFalse(result.allowed());
        verify(bucket, never()).asBlocking();
    }

    @Test
    void allowsWhenBucketIsMissingAndPolicyIsAllow() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setMissingBucketPolicy(RateLimitProperties.MissingBucketPolicy.ALLOW);

        when(rateLimitBucketService.resolveBucket("missing_bucket", "k1")).thenReturn(Optional.empty());

        Bucket4jRateLimiterUtility utility = new Bucket4jRateLimiterUtility(rateLimitBucketService, properties);

        RateLimitResult result = utility.tryConsume("missing_bucket", "k1");

        assertTrue(result.allowed());
        assertFalse(result.bucketConfigured());
    }

    private static RateLimitProperties.RateLimitDefinition definitionWithQueueWait(Duration queueWait) {
        RateLimitProperties.RateLimitDefinition definition = new RateLimitProperties.RateLimitDefinition();
        definition.setTokenCapacity(10);
        RateLimitProperties.RefillIntervally refill = new RateLimitProperties.RefillIntervally();
        refill.setToken(10);
        refill.setPeriodSeconds(60);
        definition.setRefillIntervally(refill);
        definition.setMaxWaitDuration(queueWait);
        return definition;
    }
}
