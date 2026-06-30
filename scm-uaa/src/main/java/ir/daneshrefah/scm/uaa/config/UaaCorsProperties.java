package ir.daneshrefah.scm.uaa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.uaa.cors")
public class UaaCorsProperties {
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
    private boolean allowCredentials = true;
    private List<String> pathPatterns = new ArrayList<>(List.of("/api/**"));
}
