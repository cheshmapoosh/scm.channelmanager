package ir.daneshrefah.scm.provider.rest.token;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig;

public interface ProviderAuthTokenProvider {

    ProviderAuthToken resolveToken(
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    );
}
