package ir.daneshrefah.scm.cache.starter.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties("scm.rate-limit.config")
public class RateLimitProperties {

    private MissingBucketPolicy missingBucketPolicy = MissingBucketPolicy.REJECT;
    private OverflowPolicy overflowPolicy = OverflowPolicy.REJECT;
    private Duration maxWaitDuration = Duration.ZERO;
    private Map<String, RateLimitDefinition> definitions;

    public enum MissingBucketPolicy {
        REJECT, ALLOW
    }

    public enum OverflowPolicy {
        REJECT, WAIT
    }

    @Setter
    @Getter
    public static class RateLimitDefinition {
        private Integer tokenCapacity;
        private RefillIntervally refillIntervally;
        private Duration maxWaitDuration;
    }

    @Setter
    @Getter
    public static class RefillIntervally {
        private Integer token;
        private Integer periodSeconds;
    }

}
