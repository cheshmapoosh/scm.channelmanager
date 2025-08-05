package ir.daneshrefah.scm.cache.client.distribution.spec;

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
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedRateLimiterServiceImpl implements DistributedRateLimiterService {

    private static final String BUCKET_MAP_NAME = "rate-limit-buckets";
    private static final Map<String, Supplier<BucketConfiguration>> rateLimitConfigs = new ConcurrentHashMap<>();
    private static final Set<String> registerBuckets = new HashSet<>();
    private final HazelcastInstance hazelcastInstance;
    private final List<ServiceBucketDefinition> bucketDefinitionList;
    private final RateLimitProperties rateLimitProperties;
    private ProxyManager<String> proxyManager;

    @PostConstruct
    public void configure() {
        log.info("Configuring DistributedRateLimiterService ...");
        IMap<String, byte[]> map = hazelcastInstance.getMap(BUCKET_MAP_NAME);
        this.proxyManager = Bucket4jHazelcast.entryProcessorBasedBuilder(map).build();
        configureBuckets();
        log.info("Distributed rate limit service configured successfully.");
    }

    private void configureBuckets() {
        if (Objects.nonNull(rateLimitProperties)) {
            RateLimitProperties.Type type = rateLimitProperties.getType();
            if (type.equals(RateLimitProperties.Type.FIRST_CODE)) {
                registerCodeDefinedBucket();
                registerPropertiesDefinedBucket();
            } else {
                registerPropertiesDefinedBucket();
                registerCodeDefinedBucket();
            }
        } else {
            registerCodeDefinedBucket();
        }
    }

    private void registerCodeDefinedBucket() {
        bucketDefinitionList.forEach(bucketDefinition -> {
            if (registerBuckets.add(bucketDefinition.serviceBucketName())) {
                rateLimitConfigs.put(bucketDefinition.serviceBucketName(), bucketDefinition.configuration());
                log.info(">>> create code based bucket definition '{}' successfully", bucketDefinition.serviceBucketName());
            }
        });
    }

    private void registerPropertiesDefinedBucket() {
        if (Objects.isNull(rateLimitProperties.getDefinitions())) {
            return;
        }
        rateLimitProperties
                .getDefinitions()
                .forEach((serviceBucketName, rateLimitDefinition) -> {
                    if (registerBuckets.add(serviceBucketName)) {
                        RateLimitProperties.RefillIntervally intervally = rateLimitDefinition.getRefillIntervally();
                        Bandwidth bandwidth = Bandwidth.builder()
                                .capacity(rateLimitDefinition.getTokenCapacity())
                                .refillIntervally(intervally.getToken(), Duration.ofSeconds(intervally.getPeriodSeconds()))
                                .build();
                        rateLimitConfigs.put(serviceBucketName, () ->
                                BucketConfiguration
                                        .builder()
                                        .addLimit(bandwidth)
                                        .build());
                        log.info(">>> create properties based bucket definition '{}' successfully", serviceBucketName);
                    }
                });
    }

    public Optional<Bucket> resolveBucket(String serviceName, String key) {
        Supplier<BucketConfiguration> bucketConfiguration =
                rateLimitConfigs.getOrDefault(serviceName, null);
        if (Objects.isNull(bucketConfiguration)) {
            return Optional.empty();
        }
        return Optional.of(proxyManager.builder().build(key, bucketConfiguration));
    }

    @Override
    public boolean tryConsume(String serviceName, String key, int tokenCountUsage) {
        return resolveBucket(serviceName, key)
                .map(bucket -> bucket.tryConsume(tokenCountUsage)).orElse(false);
    }

    @Override
    public boolean tryConsume(String serviceName, String key) {
        return tryConsume(serviceName, key, 1);
    }
}
