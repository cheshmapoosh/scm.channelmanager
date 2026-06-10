package ir.daneshrefah.scm.cache.client.utility.ratelimit.backend;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.Bucket4jHazelcast;
import ir.daneshrefah.scm.cache.client.config.properties.RateLimitProperties;
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
public class HazelcastRateLimitBucketService implements RateLimitBucketService {

    private static final String BUCKET_MAP_NAME = "rate-limit-buckets";
    private static final String DEFAULT_BUCKET_KEY = "global";

    private final Map<String, Supplier<BucketConfiguration>> rateLimitConfigurations = new ConcurrentHashMap<>();
    private final HazelcastInstance hazelcastInstance;
    private final RateLimitProperties rateLimitProperties;
    private ProxyManager<String> proxyManager;

    @PostConstruct
    public void configure() {
        log.info("Configuring HazelcastRateLimitBucketService ...");
        IMap<String, byte[]> map = hazelcastInstance.getMap(BUCKET_MAP_NAME);
        this.proxyManager = Bucket4jHazelcast.entryProcessorBasedBuilder(map).build();
        configureBuckets();
        log.info("HazelcastRateLimitBucketService configured with {} bucket definitions", rateLimitConfigurations.size());
    }

    @Override
    public Optional<Bucket> resolveBucket(String bucketName, String key) {
        Supplier<BucketConfiguration> bucketConfiguration = rateLimitConfigurations.get(bucketName);
        if (bucketConfiguration == null) {
            log.debug("No bucket configuration found for bucket='{}'", bucketName);
            return Optional.empty();
        }
        String normalizedKey = normalizeBucketKey(bucketName, key);
        return Optional.of(proxyManager.builder().build(normalizedKey, bucketConfiguration));
    }

    private void configureBuckets() {
        registerPropertiesDefinedBuckets();
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
            registerBucket(bucketName, () ->
                    BucketConfiguration.builder().addLimit(bandwidth).build());
        });
    }

    private void registerBucket(String bucketName, Supplier<BucketConfiguration> configuration) {
        rateLimitConfigurations.put(bucketName, configuration);
        log.info("Properties bucket definition '{}' registered successfully", bucketName);
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
