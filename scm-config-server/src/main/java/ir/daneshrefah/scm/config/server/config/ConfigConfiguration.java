package ir.daneshrefah.scm.config.server.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "scm.config")
@Data
public class ConfigConfiguration {
    private Map<String, Set<String>> applications;

}
