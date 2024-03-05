package ir.daneshrefah.scm.notification.client.config.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.notification.producer")
@Component
public class Producer {
    private boolean enabled;
    private Mode mode;

    public enum Mode {
        DISTRIBUTED,SINGLE_MODULE
    }
}
