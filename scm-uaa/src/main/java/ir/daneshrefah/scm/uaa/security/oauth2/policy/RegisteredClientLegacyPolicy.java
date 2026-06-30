package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Central place for client-aware legacy grant enablement decisions.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class RegisteredClientLegacyPolicy {
    public static final String SETTING_LEGACY_ENABLED = "scm.uaa.legacy.enabled";
    public static final String SETTING_LEGACY_CLIENT_TYPE = "scm.uaa.legacy.client.type";
    public static final String SETTING_LEGACY_PWA_COOKIE_ENABLED = "scm.uaa.legacy.pwa.cookie.enabled";
    public static final String SETTING_LEGACY_NIB_BACK_TO_BACK_ENABLED = "scm.uaa.legacy.nib.back-to-back.enabled";

    private final LegacyAuthProperties properties;

    public boolean legacyPasswordGrantEnabled() {
        return properties.isEnabled();
    }

    public boolean isLegacyPasswordGrantAllowed(RegisteredClient registeredClient, LegacyClientType clientType) {
        if (!properties.isEnabled() || registeredClient == null || clientType == null) {
            return false;
        }
        return clientMatches(registeredClient, clientType)
                && setting(registeredClient, SETTING_LEGACY_ENABLED, true);
    }

    public boolean isPwaCookieAllowed(RegisteredClient registeredClient, LegacyClientType clientType) {
        if (!isLegacyPasswordGrantAllowed(registeredClient, clientType) || !LegacyClientType.PWA.equals(clientType)) {
            return false;
        }
        return setting(
                registeredClient,
                SETTING_LEGACY_PWA_COOKIE_ENABLED,
                properties.getPwa().getCookie().isEnabled()
        );
    }

    public boolean isMbHeaderOnly(RegisteredClient registeredClient, LegacyClientType clientType) {
        return isLegacyPasswordGrantAllowed(registeredClient, clientType)
                && LegacyClientType.MB.equals(clientType)
                && clientMatches(registeredClient, LegacyClientType.MB);
    }

    public boolean isNibBackToBackAllowed(RegisteredClient registeredClient, LegacyClientType clientType) {
        if (!isLegacyPasswordGrantAllowed(registeredClient, clientType) || !LegacyClientType.NIB.equals(clientType)) {
            return false;
        }
        return setting(
                registeredClient,
                SETTING_LEGACY_NIB_BACK_TO_BACK_ENABLED,
                clientMatches(registeredClient, LegacyClientType.NIB)
        );
    }

    private boolean clientMatches(RegisteredClient registeredClient, LegacyClientType clientType) {
        LegacyClientType configuredType = configuredClientType(registeredClient);
        if (configuredType != null) {
            return configuredType == clientType;
        }
        String clientId = registeredClient.getClientId();
        if (!StringUtils.hasText(clientId)) {
            return false;
        }
        String normalized = clientId.trim().toUpperCase(Locale.ROOT);
        return switch (clientType) {
            case PWA -> normalized.startsWith("PWA");
            case MB -> normalized.startsWith("MB");
            case NIB -> normalized.startsWith("NIB");
        };
    }

    private LegacyClientType configuredClientType(RegisteredClient registeredClient) {
        if (registeredClient.getClientSettings() == null) {
            return null;
        }
        Object setting = registeredClient.getClientSettings().getSettings().get(SETTING_LEGACY_CLIENT_TYPE);
        if (setting == null) {
            return null;
        }
        String text = String.valueOf(setting).trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return LegacyClientType.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean setting(RegisteredClient registeredClient, String key, boolean defaultValue) {
        if (registeredClient.getClientSettings() == null) {
            return defaultValue;
        }
        Object value = registeredClient.getClientSettings().getSettings().get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
