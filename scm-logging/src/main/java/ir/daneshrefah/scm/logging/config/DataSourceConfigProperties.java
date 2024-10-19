package ir.daneshrefah.scm.logging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConditionalOnProperty(value = "scm.logging.datasource.enabled",havingValue = "true")
public class DataSourceConfigProperties {

    private DatasourceProperties datasource = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }
}