package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;

public interface ShetabPortLeaseManager {
    ShetabPortLease acquire(ShetabResolvedConfig config);
}
