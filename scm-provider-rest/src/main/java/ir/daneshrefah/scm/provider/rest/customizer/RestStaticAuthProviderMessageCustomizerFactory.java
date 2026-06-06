package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Component
public class RestStaticAuthProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<RestStaticAuthProviderMessageCustomizerFactory.Config> {
    public static final String TYPE = "rest-static-auth";
    public static final int ORDER = 5000;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Class<Config> configType() {
        return Config.class;
    }

    @Override
    public int defaultOrder() {
        return ORDER;
    }

    @Override
    public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
        if (context == null || !"rest".equalsIgnoreCase(context.transportType())) {
            throw new IllegalArgumentException("rest-static-auth customizer can only be configured for REST providers");
        }
        Config safe = config == null ? new Config() : config;
        safe.validate(context.providerCode());
        return new StaticAuthCustomizer(
                safe.authType(),
                StringUtils.defaultIfBlank(safe.headerName, HttpHeaders.AUTHORIZATION),
                safe.prefix,
                safe.username,
                safe.password,
                safe.token,
                safe.basicBase64 == null || safe.basicBase64,
                ORDER
        );
    }

    @Getter
    @Setter
    public static class Config {
        private String type = "NONE";
        private String headerName = HttpHeaders.AUTHORIZATION;
        private String prefix;
        private String username;
        private String password;
        private String token;
        private Boolean basicBase64 = true;

        StaticAuthType authType() {
            try {
                return StaticAuthType.valueOf(StringUtils.defaultIfBlank(type, "NONE").trim().toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported rest-static-auth.type: " + type);
            }
        }

        void validate(String providerCode) {
            if (StringUtils.isBlank(headerName)) {
                throw new IllegalArgumentException("rest-static-auth.header-name is required for provider " + providerCode);
            }
            StaticAuthType resolved = authType();
            if (resolved == StaticAuthType.BASIC && (StringUtils.isBlank(username) || StringUtils.isBlank(password))) {
                throw new IllegalArgumentException("rest-static-auth BASIC requires username and password for provider " + providerCode);
            }
            if ((resolved == StaticAuthType.BEARER || resolved == StaticAuthType.JWT || resolved == StaticAuthType.API_KEY)
                    && StringUtils.isBlank(token)) {
                throw new IllegalArgumentException("rest-static-auth token is required for provider " + providerCode);
            }
        }
    }

    enum StaticAuthType {
        BASIC,
        BEARER,
        JWT,
        API_KEY,
        NONE
    }

    record StaticAuthCustomizer(
            StaticAuthType type,
            String headerName,
            String prefix,
            String username,
            String password,
            String token,
            boolean basicBase64,
            int order
    ) implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            String value = switch (type) {
                case BASIC -> basicHeader();
                case BEARER -> withPrefix(prefix, token, "Bearer");
                case JWT -> withPrefix(prefix, token, "JWT");
                case API_KEY -> withPrefix(prefix, token, null);
                case NONE -> null;
            };
            if (StringUtils.isNotBlank(value)) {
                exchange.request().putHeader(headerName, value);
            }
        }

        private String basicHeader() {
            String credentials = username + ":" + password;
            if (basicBase64) {
                credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            }
            return StringUtils.defaultIfBlank(prefix, "Basic") + " " + credentials;
        }

        private String withPrefix(String configuredPrefix, String value, String defaultPrefix) {
            String resolvedPrefix = StringUtils.defaultIfBlank(configuredPrefix, defaultPrefix);
            if (StringUtils.isBlank(resolvedPrefix)) {
                return value;
            }
            return resolvedPrefix + " " + value;
        }
    }
}
