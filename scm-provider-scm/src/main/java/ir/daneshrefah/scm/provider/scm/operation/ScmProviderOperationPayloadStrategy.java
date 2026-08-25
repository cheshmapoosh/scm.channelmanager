package ir.daneshrefah.scm.provider.scm.operation;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.provider.operation.ProviderOperationPayloadStrategy;
import ir.daneshrefah.scm.provider.scm.camel.ScmComponent;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceRegistry;

public final class ScmProviderOperationPayloadStrategy implements ProviderOperationPayloadStrategy {

    private final ScmResourceRegistry resourceRegistry;

    public ScmProviderOperationPayloadStrategy(ScmResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public boolean supports(String scheme) {
        return ScmComponent.SCHEME.equalsIgnoreCase(scheme);
    }

    @Override
    public void validate(Operation operation) {
        resourceRegistry.validateOperation(operation);
    }

    @Override
    public Object prepareBody(Object body, Operation operation) {
        return body instanceof Message message ? message.getPayload() : body;
    }
}
