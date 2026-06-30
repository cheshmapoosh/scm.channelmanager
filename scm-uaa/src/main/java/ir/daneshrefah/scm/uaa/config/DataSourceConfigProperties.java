package ir.daneshrefah.scm.uaa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.uaa.datasource")
public class DataSourceConfigProperties {

    private DatasourceProperties main = new DatasourceProperties();

    private DatasourceProperties activation = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private boolean enabled = true;
        private int maxConnection = 10;
        private String defaultSchema;
    }

}
