package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Keeps legacy PWA cookie eligibility explicit and property-driven.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class LegacyCookiePolicy {
    private final LegacyAuthProperties properties;

    public boolean canCreateCookie(LegacyClientType clientType) {
        return properties.isEnabled()
                && LegacyClientType.PWA.equals(clientType)
                && properties.getPwa().getCookie().isEnabled();
    }
}
