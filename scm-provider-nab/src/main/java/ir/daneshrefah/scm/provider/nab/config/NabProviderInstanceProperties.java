package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NabProviderInstanceProperties {
    private String scheme;
    private Boolean enabled = true;
    private String protocol;
    private String endpoint;
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
    private List<Field> headerFields = new ArrayList<>();
    private Map<String, List<Field>> headerFieldsByProtocol = new LinkedHashMap<>();
    private RqUid rqUid = new RqUid();
    private CharacterNormalization characterNormalization = new CharacterNormalization();
    private RateLimit rateLimit = new RateLimit();
    private Boolean wireLogEnabled;
    private List<ProviderMessageCustomizerDefinition> messageCustomizers = new ArrayList<>();

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
    public static class RateLimit {
        private Boolean enabled;
        private String bucket;
        private String key;
    }
}
