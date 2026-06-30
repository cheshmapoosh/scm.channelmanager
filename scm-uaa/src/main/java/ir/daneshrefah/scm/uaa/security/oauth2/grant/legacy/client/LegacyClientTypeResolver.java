package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Legacy client resolver kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyClientTypeResolver {
    public LegacyClientType resolve(String clientId, String appVersion, AuthorizationGrantType grantType) {
        String candidate = firstText(clientId, appVersion);
        if (StringUtils.hasText(candidate)) {
            String normalized = candidate.trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("MB")) {
                return LegacyClientType.MB;
            }
            if (normalized.startsWith("PWA")) {
                return LegacyClientType.PWA;
            }
            if (normalized.startsWith("NIB")) {
                return LegacyClientType.NIB;
            }
        }
        return AuthorizationGrantType.DEFAULT.equals(grantType) ? LegacyClientType.PWA : LegacyClientType.NIB;
    }

    private String firstText(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return StringUtils.hasText(second) ? second : null;
    }
}
