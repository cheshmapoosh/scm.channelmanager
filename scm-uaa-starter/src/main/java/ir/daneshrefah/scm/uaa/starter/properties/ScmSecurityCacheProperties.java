package ir.daneshrefah.scm.uaa.starter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("scm.security.cache")
public class ScmSecurityCacheProperties {
    private Session session = new Session();
    private User user = new User();

    @Getter
    @Setter
    public static class Session {
        private boolean enabled;
        private String cacheName = "session_cache";
    }

    @Getter
    @Setter
    public static class User {
        private boolean enabled;
        private String cacheName = "user_cache";
        private String keyExpression;
    }
}
