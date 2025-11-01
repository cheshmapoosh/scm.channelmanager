package ir.daneshrefah.scm.log.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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
    private long timeToLive = Duration.ofDays(2).toMillis();
}