package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Legacy auth properties kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "scm.uaa.legacy")
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyAuthProperties {
    private boolean enabled = true;
    private final Pwa pwa = new Pwa();
    private final ClientResolution clientResolution = new ClientResolution();

    @Getter
    @Setter
    public static class ClientResolution {
        private String pwaClientId = "PWA";
        private String mbClientId = "MB";
        private String superAppClientId = "SA";
        private String nibClientId = "NIB";
    }

    @Getter
    @Setter
    public static class Pwa {
        private final Cookie cookie = new Cookie();
    }

    @Getter
    @Setter
    public static class Cookie {
        private boolean enabled = true;
        private String name = "__Host-SCM-PWA";
        private Duration maxAge = Duration.ofHours(1);
        private boolean secure = true;
        private boolean httpOnly = true;
        private String sameSite = "Lax";
        private String path = "/";
    }
}
