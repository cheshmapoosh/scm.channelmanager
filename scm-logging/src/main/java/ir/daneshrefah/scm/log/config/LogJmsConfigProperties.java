package ir.daneshrefah.scm.log.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("scm.jms.log")
@Getter
@Setter
public class LogJmsConfigProperties {
    private String queueManager;
    private String channel;
    private String host;
    private int port;
    private String username;
    private String password;
    private String destination;
}