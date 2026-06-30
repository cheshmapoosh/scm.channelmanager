package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

/**
 * Keeps legacy PWA cookie eligibility explicit and property-driven.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class LegacyCookiePolicy {
    private final RegisteredClientLegacyPolicy legacyPolicy;

    public boolean canCreateCookie(RegisteredClient registeredClient, LegacyClientType clientType) {
        return legacyPolicy.isPwaCookieAllowed(registeredClient, clientType);
    }
}
