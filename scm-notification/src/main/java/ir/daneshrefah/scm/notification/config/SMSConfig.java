package ir.daneshrefah.scm.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static ir.daneshrefah.scm.notification.config.ConfigProperties.SCM_NOTIFICATION_SMS_CONFIG;

@Setter
@Getter
@ConfigurationProperties(prefix = SCM_NOTIFICATION_SMS_CONFIG)
public class SMSConfig {
    private int maxSecondsRetryExpiration;
}
