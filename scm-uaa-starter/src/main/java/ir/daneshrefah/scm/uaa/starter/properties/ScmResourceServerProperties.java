package ir.daneshrefah.scm.uaa.starter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("scm.security.resource-server")
public class ScmResourceServerProperties {
    private boolean enabled = true;
    private String issuerUri;
    private String jwkSetUri;
    private List<String> audiences = new ArrayList<>();
    private List<String> requiredClaims = new ArrayList<>(List.of(
            "sub",
            "sid",
            "nickname",
            "terminalCode"
    ));
    private Claims claims = new Claims();
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/actuator/health",
            "/actuator/health/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    ));
    private boolean methodSecurityEnabled = true;
    private Observation observation = new Observation();
    private Token token = new Token();

    @Getter
    @Setter
    public static class Claims {
        private String subject = "sub";
        private String sessionId = "sid";
        private String nickname = "nickname";
        private String terminalCode = "terminalCode";
        private String clientId = "client_id";
        private String tokenId = "jti";
        private String roles = "roles";
        private String scopes = "scope";
    }

    @Getter
    @Setter
    public static class Observation {
        private boolean enabled = true;
        private boolean logEnabled = true;
        private boolean traceEnabled = true;
        private boolean metricEnabled = true;
    }

    @Getter
    @Setter
    public static class Token {
        private boolean cookieEnabled = false;
        private String cookieName = "__Host-SCM-PWA";
        private boolean preferCookie = false;
    }
}
