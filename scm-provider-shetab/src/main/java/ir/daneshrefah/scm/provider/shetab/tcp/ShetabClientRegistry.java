package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderTraceLifecycle;
import org.jpos.iso.ISOMsg;

public interface ShetabClientRegistry {
    ISOMsg request(ShetabResolvedConfig config, ISOMsg request);

    default ShetabTransportResponse request(
            ShetabResolvedConfig config,
            ISOMsg request,
            ShetabProviderTraceLifecycle traceLifecycle
    ) {
        return new ShetabTransportResponse(request(config, request), null);
    }
}
