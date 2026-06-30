package ir.daneshrefah.scm.uaa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.uaa.cors")
public class UaaCorsProperties implements EnvironmentAware, InitializingBean {
    private static final Set<String> STRICT_PROFILES = Set.of("test", "pilot", "prod");

    private boolean enabled = true;
    private List<String> allowedOriginPatterns = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    ));
    private List<String> allowedHeaders = new ArrayList<>(List.of(
            "Authorization",
            "Content-Type",
            "X-Correlation-Id",
            "AppVersion",
            "Appversion",
            "app_version",
            "app-version",
            "APP_VERSION",
            "APPVERSION",
            "x-otp-code",
            "AccessParameter",
            "Signature",
            "RegistryToken"
    ));
    private List<String> exposedHeaders = new ArrayList<>(List.of("Set-Cookie", "X-Correlation-Id"));
    private boolean allowCredentials = true;
    private List<String> pathPatterns = new ArrayList<>(List.of("/**"));
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        if (!enabled) {
            return;
        }
        List<String> configuredOrigins = allowedOriginPatterns.stream()
                .filter(origin -> origin != null && !origin.isBlank())
                .map(String::trim)
                .toList();
        if (allowCredentials && configuredOrigins.contains("*")) {
            throw new IllegalStateException(
                    "scm.uaa.cors.allowed-origin-patterns must not contain '*' when credentials are enabled"
            );
        }
        boolean strictProfile = environment != null
                && Arrays.stream(environment.getActiveProfiles()).anyMatch(STRICT_PROFILES::contains);
        if (strictProfile && configuredOrigins.isEmpty()) {
            throw new IllegalStateException(
                    "scm.uaa.cors.allowed-origin-patterns must be configured for test, pilot, and prod"
            );
        }
        allowedOriginPatterns = new ArrayList<>(configuredOrigins);
    }
}
