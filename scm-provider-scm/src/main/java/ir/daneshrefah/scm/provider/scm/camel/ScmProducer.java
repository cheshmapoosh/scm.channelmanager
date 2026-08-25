package ir.daneshrefah.scm.provider.scm.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.provider.scm.exception.ScmResourceProviderException;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceActionDescriptor;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class ScmProducer extends DefaultProducer {

    private final String resourceName;
    private final ScmResourceRegistry resourceRegistry;
    private final ScmActionInputAdapter inputAdapter;

    ScmProducer(
            ScmEndpoint endpoint,
            String resourceName,
            ScmResourceRegistry resourceRegistry,
            ObjectMapper objectMapper
    ) {
        super(endpoint);
        this.resourceName = resourceName;
        this.resourceRegistry = resourceRegistry;
        this.inputAdapter = new ScmActionInputAdapter(objectMapper);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Operation operation = exchange.getProperty(Message.OPERATION, Operation.class);
        String actionName = operation == null ? null : operation.getPath();
        ScmResourceActionDescriptor action = resourceRegistry.requireAction(resourceName, actionName);

        Object input = inputAdapter.adapt(action, exchange.getMessage().getBody());
        if (action.acceptsInput()) {
            exchange.getMessage().setBody(input);
        }

        try {
            action.invocationDelegate().process(exchange);
        } catch (Exception exception) {
            throw translateInvocationFailure(exception);
        }

        Exception failure = exchange.getException();
        if (failure != null) {
            exchange.setException(null);
            throw translateInvocationFailure(failure);
        }
    }

    private Exception translateInvocationFailure(Exception failure) {
        ScmException domainException = findDomainException(failure);
        return domainException == null
                ? ScmResourceProviderException.invocationFailed()
                : domainException;
    }

    private ScmException findDomainException(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = failure;
        while (current != null && visited.add(current)) {
            if (current instanceof ScmException scmException) {
                return scmException;
            }
            current = current.getCause();
        }
        return null;
    }
}
