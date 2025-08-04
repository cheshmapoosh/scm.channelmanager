package ir.daneshrefah.scm.cache.client.distribution.spec;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedRateLimiterServiceImpl implements DistributedRateLimiterService {

    private static final String BUCKET_MAP_NAME = "rate-limit-buckets";
    private final HazelcastInstance hazelcastInstance;
    private ProxyManager<String> proxyManager;
    private final List<ServiceBucketDefinition> bucketDefinitionList;

    private static final Map<String, Supplier<BucketConfiguration>> rateLimitConfigs = new ConcurrentHashMap<>();

    @PostConstruct
    public void configure() {
        log.info("Configuring DistributedRateLimiterService ...");
        IMap<String, byte[]> map = hazelcastInstance.getMap(BUCKET_MAP_NAME);
        this.proxyManager = new HazelcastProxyManager<>(map);
        bucketDefinitionList.forEach(bucketDefinition -> {
            rateLimitConfigs.put(bucketDefinition.serviceBucketName(), bucketDefinition.configuration());
            log.info("create bucket definition '{}' successfully", bucketDefinition.serviceBucketName());
        });
        log.info("Distributed rate limit service configured successfully.");
    }

    public Optional<Bucket> resolveBucket(String serviceName, String key) {
        Supplier<BucketConfiguration> bucketConfiguration =
                rateLimitConfigs.getOrDefault(serviceName, null);
        if (Objects.isNull(bucketConfiguration)){
            return Optional.empty();
        }
        return Optional.of(proxyManager.builder().build(key, bucketConfiguration));
    }

    @Override
    public boolean tryConsume(String serviceName, String key, int tokenCountUsage) {
        return resolveBucket(serviceName,key)
                .map(bucket -> bucket.tryConsume(tokenCountUsage)).orElse(false);
    }

    @Override
    public boolean tryConsume(String serviceName, String key) {
        return tryConsume(serviceName, key, 1);
    }
}
