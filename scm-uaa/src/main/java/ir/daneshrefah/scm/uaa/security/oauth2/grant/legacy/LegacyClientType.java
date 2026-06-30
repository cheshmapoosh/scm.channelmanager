package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

/**
 * Legacy client families kept only for old NIB/PWA/MB/SA compatibility.
 * Remove this enum after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public enum LegacyClientType {
    NIB,
    PWA,
    MB,
    SA
}
