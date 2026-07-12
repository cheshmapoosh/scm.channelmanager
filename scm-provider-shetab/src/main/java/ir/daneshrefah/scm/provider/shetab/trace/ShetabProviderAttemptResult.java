package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import org.jpos.iso.ISOMsg;

public record ShetabProviderAttemptResult(
        ISOMsg response,
        ProviderBusinessOutcome outcome,
        Throwable failure
) {
    public String responseCode() {
        return outcome == null ? null : outcome.responseCode();
    }

    public boolean successfulResponse() {
        return outcome != null && outcome.success();
    }
}
