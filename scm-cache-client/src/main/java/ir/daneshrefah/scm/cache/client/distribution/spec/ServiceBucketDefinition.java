package ir.daneshrefah.scm.cache.client.distribution.spec;

import io.github.bucket4j.BucketConfiguration;

import java.util.function.Supplier;


/**
 * <h2>ServiceBucketDefinition</h2>
 * <p>
 * This interface defines methods for getting rate limit configuration parameters.
 * It is typically used to provide values such as token capacity, refill amount, and refill period
 * for rate limiting algorithms (e.g., Token Bucket).
 * </p>
 *
 *
 * <h3>Configuration via Properties (No Implementation Needed)</h3>
 * <p>
 * <b>Alternatively, you can provide rate-limit configurations via your application properties or YAML configuration
 * without having to implement or extend this interface.</b> For example, using <code>application.yaml</code>:
 * </p>
 * <pre>
 * scm:
 *   rate-limit:
 *     config:
 *       type: first_code
 *       definitions:
 *         uaa_nib_activation:
 *           token-capacity: 10
 *           refill-intervally:
 *             token: 30
 *             period-seconds: 300
 * </pre>
 * <p>
 * When using Spring Boot or a similar framework, you can bind these configuration properties directly to your beans,
 * enabling flexible, declarative rate-limiting setup—no code changes or interface implementation required!
 * </p>
 *
 * @author Dariush Abdolahi
 * @since 1.0
 */
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
