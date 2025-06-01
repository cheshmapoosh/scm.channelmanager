package ir.daneshrefah.scm.uaa.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "scm.jms.logout")
@Configuration
public class LogoutJmsConfigProperties {
    private Boolean enabled;
    private String queueManager;
    private String channel;
    private String host;
    private int port;
    private String username;
    private String password;
    private String topic;
}
