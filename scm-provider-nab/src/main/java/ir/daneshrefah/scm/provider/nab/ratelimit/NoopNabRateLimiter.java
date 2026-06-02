package ir.daneshrefah.scm.provider.nab.ratelimit;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;

public class NoopNabRateLimiter implements NabRateLimiter {
    @Override
    public void acquire(NabResolvedConfig config, String operationName) {
        // noop
    }
}
