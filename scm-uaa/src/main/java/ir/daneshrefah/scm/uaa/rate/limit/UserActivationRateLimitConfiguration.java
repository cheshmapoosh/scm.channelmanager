package ir.daneshrefah.scm.uaa.rate.limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import ir.daneshrefah.scm.cache.client.distribution.spec.ServiceBucketDefinition;
import ir.daneshrefah.scm.common.constant.ServiceBucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class UserActivationRateLimitConfiguration implements ServiceBucketDefinition {
    @Override
    public String serviceBucketName() {
        return ServiceBucket.UUA_ACTIVATION_SERVICE.getBucketName();
    }

    @Override
    public Supplier<BucketConfiguration> configuration() {
        // ONE TOKEN EXISTS AND IF USED, MUST WAIT ONE MINUTE FOR ANOTHER ONE
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofSeconds(30))
                .build();
        return () ->
                BucketConfiguration
                        .builder()
                        .addLimit(bandwidth)
                        .build();
    }
}
