package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import org.springframework.stereotype.Component;

/**
 * Legacy adapter marker kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPasswordGrantAdapter {
    public boolean supports(LegacyClientType clientType) {
        return clientType == LegacyClientType.NIB || clientType == LegacyClientType.PWA || clientType == LegacyClientType.MB;
    }
}
