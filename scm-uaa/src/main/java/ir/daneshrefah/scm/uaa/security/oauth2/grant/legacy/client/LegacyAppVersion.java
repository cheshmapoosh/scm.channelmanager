package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;

import java.util.Locale;

/**
 * Legacy app-version value kept only for old PWA/MB/SA compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public final class LegacyAppVersion {
    private final String rawValue;
    private final LegacyClientType clientType;

    public LegacyAppVersion(String rawValue) {
        this.rawValue = normalize(rawValue);
        this.clientType = classify(this.rawValue);
    }

    public boolean isPwa() {
        return clientType == LegacyClientType.PWA;
    }

    public boolean isMb() {
        return clientType == LegacyClientType.MB;
    }

    public boolean isSuperApp() {
        return clientType == LegacyClientType.SA;
    }

    public boolean isKnown() {
        return clientType != null;
    }

    public boolean isPresent() {
        return rawValue != null;
    }

    public String rawValue() {
        return rawValue;
    }

    public LegacyClientType clientType() {
        return clientType;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private LegacyClientType classify(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (normalized.startsWith("PWA")) {
            return LegacyClientType.PWA;
        }
        if (normalized.startsWith("MB")) {
            return LegacyClientType.MB;
        }
        if (normalized.startsWith("SA")) {
            return LegacyClientType.SA;
        }
        return null;
    }
}
