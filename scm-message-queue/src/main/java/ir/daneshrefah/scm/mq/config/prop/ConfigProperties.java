package ir.daneshrefah.scm.mq.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "scm.mq")
@Component
public class ConfigProperties {

    private boolean enabled;
    private JmsConfig ibmMq;
    private JmsConfig activeMq;


    @Data
    public static class JmsConfig {
        private String queueManager;
        private String channel;
        private String host;
        private int port;
        private String username;
        private String password;
    }

}