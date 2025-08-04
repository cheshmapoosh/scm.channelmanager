package ir.daneshrefah.scm.cache.client.distribution.spec;

import io.github.bucket4j.BucketConfiguration;

import java.util.function.Supplier;

public interface ServiceBucketDefinition {
    String serviceBucketName();

    /**
     * Creates a rate limiter bucket with the following configuration:
     * <ul>
     *   <li><b>Capacity:</b> 100 tokens (maximum 100 requests can be accumulated at any time)</li>
     *   <li><b>Refill Strategy:</b> Intervally adds 10 tokens every 2 minutes</li>
     *   <li>
     *     <b>Behavior:</b> When a request arrives, it consumes 1 token from the bucket.
     *     If the bucket is empty (no tokens left), requests will be rate-limited (denied) until enough tokens are refilled.
     *     The bucket refills up to 10 tokens every 2 minutes, up to the maximum capacity of 100 tokens.
     *   </li>
     * </ul>
     *
     * <p>
     * Example scenario:
     * <ul>
     *   <li>The service can handle up to 100 requests in quick succession.</li>
     *   <li>Once all tokens are consumed, only 10 more requests are allowed every 2 minutes.</li>
     * </ul>
     */

    Supplier<BucketConfiguration> configuration();
}
