package ir.daneshrefah.scm.provider.rest.ratelimit;

import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;

public class NoopRestProviderRateLimiter implements RestProviderRateLimiter {
    @Override
    public void acquire(RestProviderResolvedConfig config, String operationName) {
    }
}
