package ir.daneshrefah.scm.core.integration.service.guard;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scm.runtime.channel-affinity")
public class RuntimeChannelProperties {
    private boolean enabled = false;
    private List<String> allowedChannelCodes = new ArrayList<>(List.of("*"));
}
