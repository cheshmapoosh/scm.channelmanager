package ir.daneshrefah.scm.notification.consumer.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.notification.consumer.database-queue-config")
@Component
public class DatabaseQueueConfig {
    private int maxSendingStatusMinute;
}
