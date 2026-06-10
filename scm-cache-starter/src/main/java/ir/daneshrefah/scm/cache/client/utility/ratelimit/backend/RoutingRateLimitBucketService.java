package ir.daneshrefah.scm.cache.client.utility.ratelimit.backend;

import io.github.bucket4j.Bucket;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class RoutingRateLimitBucketService implements RateLimitBucketService {

    private final RateLimitBucketService localBucketService;
    private final RateLimitBucketService remoteBucketService;
    private final CacheClientProperties.UtilityBackends utilityBackends;

    @Override
    public Optional<Bucket> resolveBucket(String bucketName, String key) {
        CacheClientProperties.UtilityBackendType backendType = utilityBackends.resolveRateLimit(bucketName);
        log.debug("Rate-limit bucket '{}' resolved to {} backend", bucketName, backendType);
        return switch (backendType) {
            case LOCAL -> localBucketService.resolveBucket(bucketName, key);
            case REMOTE -> remoteBucketService().resolveBucket(bucketName, key);
        };
    }

    private RateLimitBucketService remoteBucketService() {
        if (remoteBucketService == null) {
            throw new IllegalStateException("HazelcastInstance is required for REMOTE rate-limit backend");
        }
        return remoteBucketService;
    }
}
