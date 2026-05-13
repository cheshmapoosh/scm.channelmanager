package ir.daneshrefah.scm.provider.shetab.ratelimit;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;

public interface ShetabRateLimiter {
    void acquire(ShetabResolvedConfig config, String operationName);
}
