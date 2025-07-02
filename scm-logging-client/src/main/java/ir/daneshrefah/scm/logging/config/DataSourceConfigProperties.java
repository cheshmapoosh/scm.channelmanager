package ir.daneshrefah.scm.logging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scm.logging")
//    @ConditionalOnProperty(value = "scm.logging.datasource.enabled",havingValue = "true")
public class DataSourceConfigProperties {
    private DatasourceProperties datasource = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private Boolean enabled;
        private int maxConnection;
        private String defaultSchema;
    }
}