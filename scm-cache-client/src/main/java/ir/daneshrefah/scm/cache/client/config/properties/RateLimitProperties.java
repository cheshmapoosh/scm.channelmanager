package ir.daneshrefah.scm.cache.client.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Getter
@ConfigurationProperties("scm.rate-limit.config")
@Component
public class RateLimitProperties {

    private Type type = Type.FIRST_CODE;
    private Map<String, RateLimitDefinition> definitions;

    public enum Type {
        FIRST_CODE , FIRST_PROPERTIES
    }

    @Setter
    @Getter
    public static class RateLimitDefinition {
        private Integer tokenCapacity;
        private RefillIntervally refillIntervally;
    }

    @Setter
    @Getter
    public static class RefillIntervally {
        private Integer token;
        private Integer periodSeconds;
    }

}

