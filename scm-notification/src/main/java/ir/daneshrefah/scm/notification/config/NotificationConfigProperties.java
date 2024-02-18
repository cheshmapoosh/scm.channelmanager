package ir.daneshrefah.scm.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static ir.daneshrefah.scm.notification.config.ConfigProperties.SCM_NOTIFICATION_BASE_PREFIX;

@Getter
@Setter
@ConfigurationProperties(prefix = SCM_NOTIFICATION_BASE_PREFIX)
public class NotificationConfigProperties {

    private boolean enabled;
    private DatasourceProperties dataSource = new DatasourceProperties();
    private SMSConfig smsConfig;

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }

}