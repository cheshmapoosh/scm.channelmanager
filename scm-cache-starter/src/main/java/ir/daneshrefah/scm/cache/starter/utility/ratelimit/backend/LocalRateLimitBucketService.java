package ir.daneshrefah.scm.cache.starter.utility.ratelimit.backend;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import ir.daneshrefah.scm.cache.starter.config.properties.RateLimitProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public class LocalRateLimitBucketService implements RateLimitBucketService {

    private static final String DEFAULT_BUCKET_KEY = "global";

    private final Map<String, Supplier<Bucket>> rateLimitBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> bucketInstances = new ConcurrentHashMap<>();
    private final RateLimitProperties rateLimitProperties;

    @PostConstruct
    public void configure() {
        log.info("Configuring LocalRateLimitBucketService ...");
        registerPropertiesDefinedBuckets();
        log.info("LocalRateLimitBucketService configured with {} bucket definitions", rateLimitBuckets.size());
    }

    @Override
    public Optional<Bucket> resolveBucket(String bucketName, String key) {
        Supplier<Bucket> bucketSupplier = rateLimitBuckets.get(bucketName);
        if (bucketSupplier == null) {
            log.debug("No local bucket configuration found for bucket='{}'", bucketName);
            return Optional.empty();
        }
        return Optional.of(bucketInstances.computeIfAbsent(normalizeBucketKey(bucketName, key), ignored -> bucketSupplier.get()));
    }

    private void registerPropertiesDefinedBuckets() {
        if (rateLimitProperties == null || rateLimitProperties.getDefinitions() == null) {
            return;
        }
        rateLimitProperties.getDefinitions().forEach((bucketName, definition) -> {
            if (!isValidDefinition(bucketName, definition)) {
                return;
            }
            RateLimitProperties.RefillIntervally interval = definition.getRefillIntervally();
            Bandwidth bandwidth = Bandwidth.builder()
                    .capacity(definition.getTokenCapacity())
                    .refillIntervally(interval.getToken(), Duration.ofSeconds(interval.getPeriodSeconds()))
                    .build();
            rateLimitBuckets.put(bucketName, () -> Bucket.builder().addLimit(bandwidth).build());
            log.info("Local properties bucket definition '{}' registered successfully", bucketName);
        });
    }

    private String normalizeBucketKey(String bucketName, String key) {
        String keyPart = StringUtils.hasText(key) ? key.trim() : DEFAULT_BUCKET_KEY;
        return bucketName + "::" + keyPart;
    }

    private boolean isValidDefinition(String bucketName, RateLimitProperties.RateLimitDefinition definition) {
        if (!StringUtils.hasText(bucketName)) {
            log.warn("Rate-limit definition has empty bucket name and will be ignored");
            return false;
        }
        if (definition == null) {
            log.warn("Rate-limit definition '{}' is null and will be ignored", bucketName);
            return false;
        }
        RateLimitProperties.RefillIntervally interval = definition.getRefillIntervally();
        if (definition.getTokenCapacity() == null || definition.getTokenCapacity() <= 0) {
            log.warn("Rate-limit definition '{}' has invalid token-capacity and will be ignored", bucketName);
            return false;
        }
        if (interval == null || interval.getToken() == null || interval.getToken() <= 0
                || interval.getPeriodSeconds() == null || interval.getPeriodSeconds() <= 0) {
            log.warn("Rate-limit definition '{}' has invalid refill-intervally and will be ignored", bucketName);
            return false;
        }
        return true;
    }
}
