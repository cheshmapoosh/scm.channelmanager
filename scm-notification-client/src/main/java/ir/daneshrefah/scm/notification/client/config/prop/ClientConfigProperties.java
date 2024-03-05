package ir.daneshrefah.scm.notification.client.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.notification")
@Component
public class ClientConfigProperties {

    private Producer producer;
    private DatasourceProperties dataSource = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }

}