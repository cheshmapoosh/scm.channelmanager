package ir.daneshrefah.scm.provider.shetab.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.provider.shetab")
public class ShetabProperties {
    private boolean enabled = true;
    private Instance defaults = new Instance();
    private Map<String, Instance> providers = new HashMap<>();

    @Getter
    @Setter
    public static class Instance {
        private String endpoint;
        private List<String> endpoints = new ArrayList<>();
        private String packagerClass;
        private String packagerXml;
        private Integer connectTimeoutMs = 3000;
        private Integer socketTimeoutMs = 1000;
        private Integer responseTimeoutMs = 6000;
        private Integer sendTimeoutMs = 1000;
        private Integer reconnectDelayMs = 1000;
        private Integer sameEndpointReconnectAttempts;
        private Integer queueCapacity = 1000;
        private RateLimit rateLimit = new RateLimit();
        private EndpointLease endpointLease = new EndpointLease();
        private Security security = new Security();
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Boolean enabled = false;
        private String bucket = "shetab-default";
        private String key = "provider";
    }

    @Getter
    @Setter
    public static class EndpointLease {
        private Boolean enabled = true;
        private Long ttlMs = 30000L;
    }

    @Getter
    @Setter
    public static class Security {
        private Pin pin = new Pin();
        private Mac mac = new Mac();
        private Expiry expiry = new Expiry();
        private Cvv2 cvv2 = new Cvv2();
    }

    @Getter
    @Setter
    public static class Pin {
        private Boolean enabled;
        private String key;
        private Integer field = 52;
        private Integer panField = 2;
    }

    @Getter
    @Setter
    public static class Mac {
        private Boolean enabled;
        private String key;
        private Integer field = 128;
        private Boolean verifyResponse;
        private String placeholder = "AAAAAAAAAAAAAAAA";
        private Integer packedLengthBytes = 16;
    }

    @Getter
    @Setter
    public static class Expiry {
        private Boolean enabled;
        private Integer field = 14;
    }

    @Getter
    @Setter
    public static class Cvv2 {
        private Boolean enabled;
        private Integer field = 48;
        private String tag = "P92";
        private Integer lengthDigits = 3;
        private Integer minLength = 3;
        private Integer maxLength = 4;
    }
}
