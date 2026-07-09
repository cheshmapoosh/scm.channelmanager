package ir.daneshrefah.scm.cache.starter.utility.ratelimit.backend;

import io.github.bucket4j.Bucket;

import java.util.Optional;

public interface RateLimitBucketService {

    Optional<Bucket> resolveBucket(String bucketName, String key);
}
