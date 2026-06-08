package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderConfigResolver;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthUrlProviderMessageCustomizerFactory
        implements ProviderMessageCustomizerFactory<RestAuthUrlProviderMessageCustomizerConfig> {
    public static final String TYPE = "rest-auth-url";
    public static final int ORDER = 5000;

    private final ProviderAuthTokenProvider tokenProvider;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Class<RestAuthUrlProviderMessageCustomizerConfig> configType() {
        return RestAuthUrlProviderMessageCustomizerConfig.class;
    }

    @Override
    public int defaultOrder() {
        return ORDER;
    }

    @Override
    public ProviderMessageCustomizer create(
            ProviderMessageCustomizerFactoryContext context,
            RestAuthUrlProviderMessageCustomizerConfig config
    ) {
        if (context == null || !RestProviderConfigResolver.COMPONENT_SCHEME.equalsIgnoreCase(context.scheme())) {
            throw new IllegalArgumentException("rest-auth-url customizer can only be configured for REST providers");
        }
        RestAuthUrlProviderMessageCustomizerConfig safeConfig = config == null
                ? new RestAuthUrlProviderMessageCustomizerConfig()
                : config;
        safeConfig.validate(context.providerCode());
        return new RestAuthUrlProviderMessageCustomizer(tokenProvider, safeConfig);
    }
}
