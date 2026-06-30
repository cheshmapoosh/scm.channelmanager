package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import org.springframework.stereotype.Component;

/**
 * Legacy MB delivery marker kept only for old MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyMbHeaderTokenDeliveryStrategy implements LegacyTokenDeliveryStrategy {
    @Override
    public boolean supports(LegacyClientType clientType) {
        return clientType == LegacyClientType.MB;
    }

    @Override
    public void deliver(LegacyTokenDeliveryContext context) {
        // MB remains backward-compatible with the existing token response/header behavior; no cookie is created.
    }
}
