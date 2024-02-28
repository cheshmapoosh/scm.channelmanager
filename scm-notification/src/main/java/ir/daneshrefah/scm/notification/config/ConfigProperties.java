package ir.daneshrefah.scm.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = ConfigConstants.SCM_NOTIFICATION_BASE_PREFIX)
@Component
public class ConfigProperties {

    private boolean enabled;
    private boolean distributed;
    private DatasourceProperties dataSource = new DatasourceProperties();

    @Getter
    @Setter
    public static class DatasourceProperties extends DataSourceProperties {
        private int maxConnection;
        private String defaultSchema;
    }

}