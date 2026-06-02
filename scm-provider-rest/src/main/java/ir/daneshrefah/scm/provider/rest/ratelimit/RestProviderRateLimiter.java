package ir.daneshrefah.scm.provider.rest.ratelimit;

import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;

public interface RestProviderRateLimiter {
    void acquire(RestProviderResolvedConfig config, String operationName);
}
