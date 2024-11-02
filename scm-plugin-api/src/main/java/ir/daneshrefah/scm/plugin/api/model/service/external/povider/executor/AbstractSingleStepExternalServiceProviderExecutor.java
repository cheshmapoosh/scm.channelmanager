package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStep;
import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSingleStepExternalServiceProviderExecutor extends AbstractPreparedExternalServiceProviderExecutor {

    public AbstractSingleStepExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    protected void beforeCallRoute(Exchange exchange) {
        MessageOutput messageOutput = buildMessageOutput();
        Object body = extractBody(exchange, messageOutput,this::extractServiceParametersRequestBody);
        setupMessageOutput(exchange,messageOutput,body);
    }

    protected void afterCallRoute(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        exchange.getMessage().setBody(extractServiceParametersResponseBody(originalMessage, exchange.getMessage().getBody()));
    }

    protected abstract void call(TryDefinition tryDefinition);

    @Override
    protected List<CamelInvocationStep> callRoute(TryDefinition routeDefinition) {
        List<CamelInvocationStep> steps = new ArrayList<>();
        steps.add(new CamelInvocationStep() {
            @Override
            public void beforeStepRouteCalling(Exchange exchange) {
                beforeCallRoute(exchange);
            }

            @Override
            public void callStepRoute(TryDefinition routeDefinition) {
                call(routeDefinition);
            }

            @Override
            public void afterStepRouteCalling(Exchange exchange) {
                afterCallRoute(exchange);
            }
        });
        return steps;
    }

    protected abstract MessageOutput buildMessageOutput();
    protected abstract Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput);
    protected abstract Object extractServiceParametersResponseBody(Message message, Object body);

}
