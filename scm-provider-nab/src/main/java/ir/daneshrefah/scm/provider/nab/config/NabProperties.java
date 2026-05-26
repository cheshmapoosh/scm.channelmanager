package ir.daneshrefah.scm.provider.nab.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.provider.nab")
public class NabProperties {
    private boolean enabled = true;
    private Instance defaults = new Instance();
    private Map<String, Instance> providers = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Instance {
        private List<String> endpoints = new ArrayList<>();
        private Integer connectTimeoutMs;
        private Integer socketTimeoutMs;
        private Integer responseTimeoutMs;
        private Integer responseIdleTimeoutMs;
        private Integer ackLengthBytes;
        private String charset;
        private String userId;
        private String password;
        private String defaultServiceCode;
        private Map<String, String> serviceCodesByTerminalType = new LinkedHashMap<>();
        private Map<String, String> serviceCodesByChannelCode = new LinkedHashMap<>();
        private Map<String, List<Field>> headerFieldsByProtocol = new LinkedHashMap<>();
        private RqUid rqUid = new RqUid();
        private CharacterNormalization characterNormalization = new CharacterNormalization();
        private ConnectionPool connectionPool = new ConnectionPool();
        private Boolean wireLogEnabled;
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
}
