package ir.daneshrefah.scm.uaa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "scm.otp.jms.avacas")
@Configuration
public class OtpDeviceProperties {
    private String hostName;
    private int port;
    private String channel;
    private String queueManager;
    private String username;
    private String password;
    private String sendQueueName;
    private String receiverQueueName;
}
