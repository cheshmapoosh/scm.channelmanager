package ir.daneshrefah.scm.provider.rest.token;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;

public interface ProviderAuthTokenProvider {

    ProviderAuthToken resolveToken(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context);
}
