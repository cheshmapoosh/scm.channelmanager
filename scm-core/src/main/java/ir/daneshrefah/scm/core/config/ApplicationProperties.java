package ir.daneshrefah.scm.core.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.daneshrefah.scm.core.serializer.StringToListDeserializer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("scm")
@Setter
@Getter
public class ApplicationProperties {

    private String profile;
    @Value("${scm.channels:}")
    @JsonDeserialize(converter = StringToListDeserializer.class)
    private List<String> channels;
    private DataSourceConfig datasource;
    private SecurityConfig security;

    @Getter
    @Setter
    public static class DataSourceConfig {
        private DatasourceProperties primary;
        private List<DatasourceProperties> secondary;
    }

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }

    @Getter
    @Setter
    public static class SecurityConfig {
        private String clientId;
        private String clientSecret;
        private boolean distributed;
        private String issuerUri;
    }

}
