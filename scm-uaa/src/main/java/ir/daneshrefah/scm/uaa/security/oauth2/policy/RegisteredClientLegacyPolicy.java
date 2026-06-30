package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * Central place for client-aware legacy grant enablement decisions.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class RegisteredClientLegacyPolicy {
    public static final String SETTING_LEGACY_ENABLED = "scm.uaa.legacy.enabled";
    public static final String SETTING_LEGACY_CLIENT_TYPE = LegacyClientTypeResolver.SETTING_LEGACY_CLIENT_TYPE;
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
        ConfiguredClientType configuredType = configuredClientType(registeredClient);
        if (configuredType.present()) {
            return configuredType.value() == clientType;
        }
        String clientId = registeredClient.getClientId();
        if (!StringUtils.hasText(clientId)) {
            return false;
        }
        String normalized = clientId.trim().toUpperCase(Locale.ROOT);
        return switch (clientType) {
            case PWA -> matchesConfiguredClientId(clientId, properties.getClientResolution().getPwaClientId())
                    || normalized.startsWith("PWA");
            case MB -> matchesConfiguredClientId(clientId, properties.getClientResolution().getMbClientId())
                    || normalized.startsWith("MB");
            case SA -> matchesConfiguredClientId(clientId, properties.getClientResolution().getSuperAppClientId())
                    || normalized.startsWith("SA");
            case NIB -> matchesConfiguredClientId(clientId, properties.getClientResolution().getNibClientId())
                    || normalized.startsWith("NIB");
        };
    }

    private ConfiguredClientType configuredClientType(RegisteredClient registeredClient) {
        if (registeredClient.getClientSettings() == null) {
            return ConfiguredClientType.absent();
        }
        Map<String, Object> settings = registeredClient.getClientSettings().getSettings();
        if (!settings.containsKey(SETTING_LEGACY_CLIENT_TYPE)) {
            return ConfiguredClientType.absent();
        }
        Object setting = settings.get(SETTING_LEGACY_CLIENT_TYPE);
        if (setting == null || !StringUtils.hasText(String.valueOf(setting))) {
            return ConfiguredClientType.absent();
        }
        String text = String.valueOf(setting).trim();
        String normalized = text.toUpperCase(Locale.ROOT);
        if ("SUPER_APP".equals(normalized) || "SUPER-APP".equals(normalized)) {
            return new ConfiguredClientType(true, LegacyClientType.SA);
        }
        try {
            return new ConfiguredClientType(true, LegacyClientType.valueOf(normalized));
        } catch (IllegalArgumentException ignored) {
            return new ConfiguredClientType(true, null);
        }
    }

    private boolean matchesConfiguredClientId(String actual, String configured) {
        return StringUtils.hasText(configured) && actual.trim().equalsIgnoreCase(configured.trim());
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

    private record ConfiguredClientType(boolean present, LegacyClientType value) {
        private static ConfiguredClientType absent() {
            return new ConfiguredClientType(false, null);
        }
    }
}
