package ir.daneshrefah.scm.provider.shetab.ratelimit;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;

public class NoopShetabRateLimiter implements ShetabRateLimiter {
    @Override
    public void acquire(ShetabResolvedConfig config, String operationName) {
    }
}
