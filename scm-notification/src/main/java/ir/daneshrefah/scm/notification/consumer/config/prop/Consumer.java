package ir.daneshrefah.scm.notification.consumer.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.notification.consumer")
@Component
public class Consumer {
    private boolean enabled;
    private Strategy strategy;
    private DatabaseQueueConfig databaseQueueConfig;

    public enum Strategy {
        DATABASE_QUEUED,MQ
    }
}
