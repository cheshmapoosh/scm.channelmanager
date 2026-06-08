package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RestProviderInstanceProperties {
    private String scheme;
    private Boolean enabled = true;
    private String baseUrl;
    private String endpoint;
    private Integer connectTimeoutMs;
    private Integer responseTimeoutMs;
    private Boolean virtualThreadsEnabled;
    private Boolean insecureSsl;
    private String followRedirects;
    private String defaultMethod;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Map<String, Object> providerConfig = new LinkedHashMap<>();
    private Proxy proxy = new Proxy();
    private Security security = new Security();
    private RateLimit rateLimit = new RateLimit();
    private List<ProviderMessageCustomizerDefinition> messageCustomizers = new ArrayList<>();

    @Getter
    @Setter
    public static class Proxy {
        private String host;
        private Integer port;
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class Security {
        private List<String> sensitiveHeaders = new ArrayList<>();
        private List<String> sensitiveBodyKeys = new ArrayList<>();
        private Integer maxBodyLogLength;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Boolean enabled;
        private String bucket;
        private String key;
    }
}
