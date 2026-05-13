package ir.daneshrefah.scm.cache.client.utility.ratelimit.backend;

import io.github.bucket4j.Bucket;
import ir.daneshrefah.scm.cache.client.config.properties.RateLimitProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalRateLimitBucketServiceTest {

    @Test
    void createsLocalBucketPerRateLimitNameAndKey() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setDefinitions(Map.of("login", definition(1, 1, 60)));
        LocalRateLimitBucketService service = new LocalRateLimitBucketService(properties);
        service.configure();

        Bucket userOneBucket = service.resolveBucket("login", "uid::1").orElseThrow();
        Bucket userTwoBucket = service.resolveBucket("login", "uid::2").orElseThrow();

        assertTrue(userOneBucket.tryConsume(1));
        assertFalse(userOneBucket.tryConsume(1));
        assertTrue(userTwoBucket.tryConsume(1));
        assertSame(userOneBucket, service.resolveBucket("login", "uid::1").orElseThrow());
    }

    private static RateLimitProperties.RateLimitDefinition definition(int capacity, int refillTokens, int periodSeconds) {
        RateLimitProperties.RateLimitDefinition definition = new RateLimitProperties.RateLimitDefinition();
        definition.setTokenCapacity(capacity);
        RateLimitProperties.RefillIntervally refill = new RateLimitProperties.RefillIntervally();
        refill.setToken(refillTokens);
        refill.setPeriodSeconds(periodSeconds);
        definition.setRefillIntervally(refill);
        return definition;
    }
}
