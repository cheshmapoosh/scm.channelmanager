package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import org.springframework.stereotype.Component;

/**
 * Legacy NIB back-to-back delivery marker kept only for old NIB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyNibBackToBackTokenDeliveryStrategy implements LegacyTokenDeliveryStrategy {
    @Override
    public boolean supports(LegacyClientType clientType) {
        return clientType == LegacyClientType.NIB;
    }

    @Override
    public void deliver(LegacyTokenDeliveryContext context) {
        // NIB legacy remains back-to-back; no cookie is created by default.
    }
}
