package ir.daneshrefah.scm.cache.client.distribution.spec;

import io.github.bucket4j.Bucket;

import java.util.Optional;

public interface DistributedRateLimiterService {

    /**
     * Resolves (or creates) a distributed rate limiting bucket for the specified service and key.
     *
     * @param serviceName Name of the service (should match bucket definition name)
     * @param key         Unique identifier for the rate limit (usually combination of serviceName and userId/IP)
     * @return Optional<Bucket>  Present if configuration exists for the service, otherwise empty
     */

    Optional<Bucket> resolveBucket(String serviceName, String key);

    boolean tryConsume(String serviceName, String key);

    boolean tryConsume(String serviceName, String key, int tokenCountUsage);
}
