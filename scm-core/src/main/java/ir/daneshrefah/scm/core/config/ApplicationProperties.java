package ir.daneshrefah.scm.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("scm")
@Setter
@Getter
public class ApplicationProperties {

    private String profile;
    private DataSourceConfig datasource;

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

}
