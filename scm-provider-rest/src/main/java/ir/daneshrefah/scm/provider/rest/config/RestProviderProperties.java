package ir.daneshrefah.scm.provider.rest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.provider.rest")
public class RestProviderProperties {
    private boolean enabled = true;
    private Instance defaults = new Instance();
    private Map<String, Instance> providers = new HashMap<>();

    @Getter
    @Setter
    public static class Instance {
        private String endpoint;
        private String baseUrl;
        private Integer connectTimeoutMs = 3000;
        private Integer responseTimeoutMs = 6000;
        private Boolean virtualThreadsEnabled = true;
        private Boolean insecureSsl = false;
        private String followRedirects = "NORMAL";
        private String defaultMethod = "POST";
        private Map<String, String> headers = new HashMap<>();
        private Map<String, Object> providerConfig = new HashMap<>();
        private Customizers customizers = new Customizers();
        private RateLimit rateLimit = new RateLimit();
        private Proxy proxy = new Proxy();
        private Auth auth = new Auth();
        private Security security = new Security();
        private Token token = new Token();
    }

    @Getter
    @Setter
    public static class Customizers {
        private Boolean authentication;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Boolean enabled;
        private String bucket;
        private String key;
    }

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
    public static class Auth {
        private String type;
        private String headerName;
        private String prefix;
        private String token;
        private String username;
        private String password;
        private Boolean basicBase64;
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
    public static class Token {
        private Boolean enabled;
        private String authProfile;
        private String credentialKey;
        private String cacheName;
        private String cacheKey;
        private String lockName;
        private Integer earlyRefreshSeconds;
        private Integer defaultExpiresInSeconds;
        private String method;
        private String url;
        private String path;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> query = new HashMap<>();
        private Map<String, String> form = new HashMap<>();
        private Map<String, Object> body = new HashMap<>();
        private Auth auth = new Auth();
        private String responseTokenField;
        private String responseExpiresInField;
        private String responseTokenTypeField;
        private String defaultTokenType;
        private Cache cache = new Cache();
        private Lock lock = new Lock();
        private Apply apply = new Apply();
    }

    @Getter
    @Setter
    public static class Cache {
        private Boolean enabled;
        private String mode;
        private String keyPrefix;
        private Duration refreshSkew;
        private Duration ttlSkew;
    }

    @Getter
    @Setter
    public static class Lock {
        private Boolean enabled;
        private String keyPrefix;
        private Duration waitTimeout;
        private Duration leaseTime;
        private Duration retryDelay;
    }

    @Getter
    @Setter
    public static class Apply {
        private String location;
        private String name;
        private String format;
    }
}
