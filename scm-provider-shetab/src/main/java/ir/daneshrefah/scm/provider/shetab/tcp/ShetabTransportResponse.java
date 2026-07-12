package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderTraceLifecycle;
import org.jpos.iso.ISOMsg;

public record ShetabTransportResponse(
        ISOMsg response,
        ShetabProviderTraceLifecycle.Attempt traceAttempt
) {
}
