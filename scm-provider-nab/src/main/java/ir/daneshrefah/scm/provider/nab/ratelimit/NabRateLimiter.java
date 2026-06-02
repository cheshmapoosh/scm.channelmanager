package ir.daneshrefah.scm.provider.nab.ratelimit;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;

public interface NabRateLimiter {
    void acquire(NabResolvedConfig config, String operationName);
}
