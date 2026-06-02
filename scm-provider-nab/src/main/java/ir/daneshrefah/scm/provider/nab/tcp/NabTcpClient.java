package ir.daneshrefah.scm.provider.nab.tcp;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.domain.NabWireRequest;

public interface NabTcpClient {
    String request(NabResolvedConfig config, NabWireRequest request);
}
