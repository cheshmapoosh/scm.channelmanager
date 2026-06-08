package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ShetabProviderInstanceProperties {
    private String scheme;
    private Boolean enabled = true;
    private String endpoint;
    private List<String> endpoints = new ArrayList<>();
    private String packagerClass;
    private String packagerXml;
    private Integer connectTimeoutMs;
    private Integer socketTimeoutMs;
    private Integer responseTimeoutMs;
    private Integer sendTimeoutMs;
    private Integer reconnectDelayMs;
    private Integer sameEndpointReconnectAttempts;
    private Integer queueCapacity;
    private Map<String, Object> providerConfig = new LinkedHashMap<>();
    private RateLimit rateLimit = new RateLimit();
    private EndpointLease endpointLease = new EndpointLease();
    private List<ProviderMessageCustomizerDefinition> messageCustomizers = new ArrayList<>();

    @Getter
    @Setter
    public static class RateLimit {
        private Boolean enabled;
        private String bucket;
        private String key;
    }

    @Getter
    @Setter
    public static class EndpointLease {
        private Boolean enabled;
        private Long ttlMs;
    }
}
