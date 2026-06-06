package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthException;
import ir.daneshrefah.scm.provider.rest.exception.RestProviderAuthFault;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthToken;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public final class RestAuthUrlProviderMessageCustomizer implements ProviderMessageCustomizer {
    private final ProviderAuthTokenProvider tokenProvider;
    private final RestAuthUrlProviderMessageCustomizerConfig config;

    RestAuthUrlProviderMessageCustomizer(
            ProviderAuthTokenProvider tokenProvider,
            RestAuthUrlProviderMessageCustomizerConfig config
    ) {
        this.tokenProvider = tokenProvider;
        this.config = config == null ? new RestAuthUrlProviderMessageCustomizerConfig() : config.copy();
    }

    @Override
    public int order() {
        return RestAuthUrlProviderMessageCustomizerFactory.ORDER;
    }

    @Override
    public void beforeSend(ProviderExchange exchange) {
        RestProviderResolvedConfig providerConfig = exchange.context()
                .resolvedProviderConfig(RestProviderResolvedConfig.class)
                .orElseThrow(() -> new RestProviderAuthException(
                        RestProviderAuthFault.PROVIDER_AUTH_FAILED,
                        exchange.context().providerCode(),
                        "REST provider configuration is unavailable"));
        ProviderAuthToken token = tokenProvider.resolveToken(providerConfig, config, exchange.context());
        applyToken(exchange, providerConfig, token);
        exchange.putAttribute("rest.auth.applied", Boolean.TRUE);
        log.debug("REST auth-url token applied provider={} service={} operation={} channel={} location={} name={}",
                exchange.context().providerCode(),
                exchange.context().serviceCode(),
                exchange.context().operationCode(),
                exchange.context().channelCode(),
                config.apply().locationType(),
                config.apply().getName());
    }

    private void applyToken(ProviderExchange exchange, RestProviderResolvedConfig providerConfig, ProviderAuthToken token) {
        if (token == null || StringUtils.isBlank(token.accessToken())) {
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_TOKEN_UNAVAILABLE,
                    providerConfig.provider(),
                    "REST provider auth token is unavailable for provider " + providerConfig.provider());
        }
        RestAuthUrlProviderMessageCustomizerConfig.Apply apply = config.apply();
        String name = StringUtils.trimToNull(apply.getName());
        if (name == null) {
            throw new RestProviderAuthException(
                    RestProviderAuthFault.PROVIDER_AUTH_FAILED,
                    providerConfig.provider(),
                    "REST provider token apply name is required for provider " + providerConfig.provider());
        }
        String value = formatToken(apply.getFormat(), token);
        switch (apply.locationType()) {
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
}
