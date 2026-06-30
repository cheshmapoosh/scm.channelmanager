package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

/**
 * Legacy token delivery strategy kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public interface LegacyTokenDeliveryStrategy {
    boolean supports(LegacyClientType clientType);

    void deliver(LegacyTokenDeliveryContext context);
}
