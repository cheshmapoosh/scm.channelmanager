package ir.daneshrefah.scm.mq.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "scm.ibm.mq")
@Component
public class IbmJmsConfigProperty {

    private boolean enabled;
    private String queueManager;
    private String channel;
    private String host;
    private int port;
    private String username;
    private String password;

}