package ir.daneshrefah.scm.uaa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.security")
public class DataSourceConfigProperties {

    private DatasourceProperties authenticationDatasource = new DatasourceProperties();

    private DatasourceProperties activationDatasource = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }

}