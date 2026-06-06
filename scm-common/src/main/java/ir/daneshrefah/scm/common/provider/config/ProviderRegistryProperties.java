package ir.daneshrefah.scm.common.provider.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "scm.providers")
public class ProviderRegistryProperties extends LinkedHashMap<String, ProviderRegistryProperties.Provider> {

    public Map<String, Provider> getProviders() {
        return this;
    }

    public void setProviders(Map<String, Provider> providers) {
        clear();
        if (providers != null) {
            putAll(providers);
        }
    }

    @Getter
    @Setter
    public static class Provider {
        private String type;
        private Boolean enabled = true;
        private String endpoint;
        private List<String> endpoints = new ArrayList<>();
        private String baseUrl;
        private String protocol;
        private String packagerClass;
        private String packagerXml;
        private Integer connectTimeoutMs;
        private Integer socketTimeoutMs;
        private Integer responseTimeoutMs;
        private Integer responseIdleTimeoutMs;
        private Integer sendTimeoutMs;
        private Integer reconnectDelayMs;
        private Integer sameEndpointReconnectAttempts;
        private Integer queueCapacity;
        private Integer ackLengthBytes;
        private String charset;
        private Boolean virtualThreadsEnabled;
        private Boolean insecureSsl;
        private String followRedirects;
        private String defaultMethod;
        private String userId;
        private String password;
        private String defaultServiceCode;
        private Map<String, String> headers = new LinkedHashMap<>();
        private Map<String, Object> providerConfig = new LinkedHashMap<>();
        private Map<String, String> serviceCodesByTerminalType = new LinkedHashMap<>();
        private Map<String, String> serviceCodesByChannelCode = new LinkedHashMap<>();
        private List<Field> headerFields = new ArrayList<>();
        private Map<String, List<Field>> headerFieldsByProtocol = new LinkedHashMap<>();
        private RateLimit rateLimit = new RateLimit();
        private EndpointLease endpointLease = new EndpointLease();
        private RqUid rqUid = new RqUid();
        private CharacterNormalization characterNormalization = new CharacterNormalization();
        private ConnectionPool connectionPool = new ConnectionPool();
        private Proxy proxy = new Proxy();
        private Auth auth = new Auth();
        private Security security = new Security();
        private Boolean wireLogEnabled;
        private List<ProviderMessageCustomizerDefinition> messageCustomizers = new ArrayList<>();
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
    public static class EndpointLease {
        private Boolean enabled;
        private Long ttlMs;
    }

    @Getter
    @Setter
    public static class RqUid {
        private Integer length;
        private String type;
    }

    @Getter
    @Setter
    public static class CharacterNormalization {
        private Boolean enabled;
        private Map<String, String> replacements = new LinkedHashMap<>();
    }

    @Getter
    @Setter
    public static class Field {
        private String name;
        private String path;
        private Integer length;
        private String type;
        private Boolean required;
        private String converter;
        private String padding;
        private String overflow;
        private Boolean trim;
    }

    @Getter
    @Setter
    public static class ConnectionPool {
        private Boolean enabled;
        private Integer maxTotal;
        private Integer maxSize;
        private Integer minIdle;
        private Integer maxIdle;
        private Integer maxWaitMs;
        private Integer borrowTimeoutMs;
        private Long maxIdleTimeMs;
        private Long minEvictableIdleTimeMs;
        private Long softMinEvictableIdleTimeMs;
        private Long timeBetweenEvictionRunsMs;
        private Long maxLifeTimeMs;
        private Boolean validationEnabled;
        private Boolean testOnBorrow;
        private Boolean testOnReturn;
        private Boolean testWhileIdle;
        private Boolean blockWhenExhausted;
        private Boolean lifo;
        private Boolean prefill;
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
}
