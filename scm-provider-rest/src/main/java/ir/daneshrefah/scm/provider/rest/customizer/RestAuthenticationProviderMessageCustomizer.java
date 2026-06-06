package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthException;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthFault;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthToken;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestAuthenticationProviderMessageCustomizer implements ProviderMessageCustomizer {
    public static final int ORDER = 5000;

    private final ProviderAuthTokenProvider tokenProvider;

    @Override
    public boolean supports(ProviderMessageCustomizerContext context) {
        if (context == null || !"rest".equalsIgnoreCase(context.transportType())) {
            return false;
        }
        return context.resolvedProviderConfig(RestProviderResolvedConfig.class)
                .filter(config -> config.customizers() != null && config.customizers().authentication())
                .filter(config -> config.token() != null && config.token().enabled())
                .filter(config -> requiresSharedToken(config.auth() == null ? null : config.auth().type()))
                .isPresent();
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public void beforeSend(ProviderExchange exchange) {
        RestProviderResolvedConfig config = exchange.context()
                .resolvedProviderConfig(RestProviderResolvedConfig.class)
                .orElseThrow(() -> new RestProviderAuthException(
                        RestProviderAuthFault.PROVIDER_AUTH_FAILED,
                        exchange.context().providerCode(),
                        "REST provider auth configuration is unavailable"));

        ProviderAuthToken token = tokenProvider.resolveToken(config, exchange.context());
        applyToken(exchange, config, token);
        exchange.putAttribute("rest.auth.applied", Boolean.TRUE);
        log.debug("REST provider auth token applied provider={} service={} operation={} channel={} location={} name={}",
                exchange.context().providerCode(),
                exchange.context().serviceCode(),
                exchange.context().operationCode(),
                exchange.context().channelCode(),
                config.token().apply().location(),
                config.token().apply().name());
    }

    private void applyToken(ProviderExchange exchange, RestProviderResolvedConfig config, ProviderAuthToken token) {
        if (token == null || StringUtils.isBlank(token.accessToken())) {
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_TOKEN_UNAVAILABLE,
                    config.provider(),
                    "REST provider auth token is unavailable for provider " + config.provider());
        }
        RestProviderResolvedConfig.TokenApply apply = config.token().apply();
        String name = StringUtils.trimToNull(apply.name());
        if (name == null) {
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_FAILED,
                    config.provider(),
                    "REST provider token apply name is required for provider " + config.provider());
        }
        String value = formatToken(apply.format(), token);
        switch (apply.location()) {
            case HEADER -> exchange.request().putHeader(name, value);
            case BODY -> exchange.request().putField(name, value);
            case QUERY -> exchange.request().putQueryParameter(name, value);
        }
    }

    private String formatToken(String format, ProviderAuthToken token) {
        String tokenType = StringUtils.defaultIfBlank(token.tokenType(), "Bearer");
        String resolvedFormat = StringUtils.defaultIfBlank(format, "{tokenType} {accessToken}");
        String resolved = resolvedFormat
                .replace("{tokenType}", tokenType)
                .replace("{accessToken}", token.accessToken())
                .replace("{token}", token.accessToken())
                .trim();
        return resolved.replaceAll("\\s+", " ");
    }

    private boolean requiresSharedToken(RestProviderResolvedConfig.AuthType type) {
        if (type == null) {
            return false;
        }
        return switch (type.name().toUpperCase(Locale.ROOT)) {
            case "BEARER", "JWT", "API_KEY" -> true;
            default -> false;
        };
    }
}
