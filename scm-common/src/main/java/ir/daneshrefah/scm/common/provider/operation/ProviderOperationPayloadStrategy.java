package ir.daneshrefah.scm.common.provider.operation;

import ir.daneshrefah.scm.common.model.operation.Operation;

/**
 * Optional, scheme-specific preparation for a PROVIDER Operation body.
 *
 * <p>The default provider behavior remains unchanged when no strategy supports
 * a scheme.</p>
 */
public interface ProviderOperationPayloadStrategy {

    boolean supports(String scheme);

    void validate(Operation operation);

    Object prepareBody(Object body, Operation operation);
}
