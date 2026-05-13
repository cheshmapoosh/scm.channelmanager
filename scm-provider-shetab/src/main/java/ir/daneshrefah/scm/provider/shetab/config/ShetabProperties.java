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
    private String podId = System.getenv().getOrDefault("HOSTNAME", "scm-web");
    private Instance defaults = new Instance();
    private Map<String, Instance> providers = new HashMap<>();

    @Getter
    @Setter
    public static class Instance {
        private String host;
        private Integer port;
        private String localAddress;
        private List<Integer> localPorts = new ArrayList<>();
        private String channelType = "ASCII";
        private Integer lengthDigits = 4;
        private String packagerClass;
        private String packagerXml;
        private Integer connectTimeoutMs = 3000;
        private Integer socketTimeoutMs = 1000;
        private Integer responseTimeoutMs = 6000;
        private Integer sendTimeoutMs = 1000;
        private Integer reconnectDelayMs = 1000;
        private Integer queueCapacity = 1000;
        private RateLimit rateLimit = new RateLimit();
        private PortLease portLease = new PortLease();
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
    public static class PortLease {
        private Boolean enabled = true;
        private Long ttlMs = 30000L;
    }
}
