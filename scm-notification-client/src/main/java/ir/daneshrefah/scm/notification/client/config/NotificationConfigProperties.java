package ir.daneshrefah.scm.notification.client.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = ConfigProperties.SCM_NOTIFICATION_BASE_PREFIX)
@Component
public class NotificationConfigProperties {

    private boolean enabled;
    private DatasourceProperties dataSource = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }

}