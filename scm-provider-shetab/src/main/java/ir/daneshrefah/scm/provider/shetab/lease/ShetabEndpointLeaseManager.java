package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;

public interface ShetabEndpointLeaseManager {
    ShetabEndpointLease acquire(ShetabResolvedConfig config);
}
