package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * Legacy client resolver kept only for old NIB/PWA/MB/SA compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyClientTypeResolver {
    public static final String SETTING_LEGACY_CLIENT_TYPE = "scm.uaa.legacy.client.type";

    private final LegacyAuthProperties properties;

    public LegacyClientTypeResolver(LegacyAuthProperties properties) {
        this.properties = properties;
    }

    public LegacyClientType resolve(
            RegisteredClient registeredClient,
            String clientId,
            LegacyAppVersion appVersion,
            AuthorizationGrantType grantType
    ) {
        ConfiguredClientType configuredClientType = configuredClientType(registeredClient);
        if (configuredClientType.present()) {
            return configuredClientType.value();
        }

        LegacyClientType clientIdType = clientIdType(clientId);
        if (clientIdType != null) {
            return clientIdType;
        }

        if (AuthorizationGrantType.DEFAULT.equals(grantType)
                && appVersion != null
                && appVersion.isKnown()) {
            return appVersion.clientType();
        }
        return null;
    }

    private ConfiguredClientType configuredClientType(RegisteredClient registeredClient) {
        if (registeredClient == null || registeredClient.getClientSettings() == null) {
            return ConfiguredClientType.absent();
        }
        Map<String, Object> settings = registeredClient.getClientSettings().getSettings();
        if (!settings.containsKey(SETTING_LEGACY_CLIENT_TYPE)) {
            return ConfiguredClientType.absent();
        }
        Object value = settings.get(SETTING_LEGACY_CLIENT_TYPE);
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return ConfiguredClientType.absent();
        }
        return new ConfiguredClientType(true, parse(String.valueOf(value)));
    }

    private LegacyClientType clientIdType(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return null;
        }
        LegacyAuthProperties.ClientResolution configured = properties.getClientResolution();
        if (equalsClientId(clientId, configured.getPwaClientId())) {
            return LegacyClientType.PWA;
        }
        if (equalsClientId(clientId, configured.getMbClientId())) {
            return LegacyClientType.MB;
        }
        if (equalsClientId(clientId, configured.getSuperAppClientId())) {
            return LegacyClientType.SA;
        }
        if (equalsClientId(clientId, configured.getNibClientId())) {
            return LegacyClientType.NIB;
        }

        String normalized = clientId.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("PWA")) {
            return LegacyClientType.PWA;
        }
        if (normalized.startsWith("MB")) {
            return LegacyClientType.MB;
        }
        if (normalized.startsWith("SA")) {
            return LegacyClientType.SA;
        }
        if (normalized.startsWith("NIB")) {
            return LegacyClientType.NIB;
        }
        return null;
    }

    private LegacyClientType parse(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("SUPER_APP".equals(normalized) || "SUPER-APP".equals(normalized)) {
            return LegacyClientType.SA;
        }
        try {
            return LegacyClientType.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean equalsClientId(String actual, String configured) {
        return StringUtils.hasText(configured) && actual.trim().equalsIgnoreCase(configured.trim());
    }

    private record ConfiguredClientType(boolean present, LegacyClientType value) {
        private static ConfiguredClientType absent() {
            return new ConfiguredClientType(false, null);
        }
    }
}
