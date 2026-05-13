package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;

public class NoopShetabPortLeaseManager implements ShetabPortLeaseManager {
    @Override
    public ShetabPortLease acquire(ShetabResolvedConfig config) {
        return ShetabPortLease.none();
    }
}
