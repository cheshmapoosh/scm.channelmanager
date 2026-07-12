package ir.daneshrefah.scm.provider.shetab.trace;

import org.jpos.iso.ISOMsg;

public record ShetabProviderAttemptResult(
        ISOMsg response,
        String responseCode,
        boolean successfulResponse,
        Throwable failure
) {
}
