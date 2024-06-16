package ir.daneshrefah.scm.notification.client.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "scm.notification")
@Component
public class ClientConfigProperties {

    private Boolean enabled;
    private SmsProperties sms;

    @Data
    public static class SmsProperties {
        private boolean enabled;
    }

}