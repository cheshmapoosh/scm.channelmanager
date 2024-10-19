package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.CamelInvocationStep;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import org.apache.camel.Exchange;
import org.apache.camel.model.TryDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractPreparedExternalServiceProviderExecutor extends AbstractBaseExternalServiceProviderExecutor {


    public AbstractPreparedExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    @Override
    protected void beforeRouteCalling(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        MessageOutput messageOutput = buildMessageOutput();
        messageOutput.setExternalCorrelationId(getProviderCorrelationId(originalMessage));
        Object body = extractRequestBody(exchange, messageOutput);
        messageOutput.setBody(body);
        exchange.getMessage().setBody(messageOutput.getBody());
        exchange.setProperty(HEADER_MESSAGE_OUTPUT, messageOutput);
    }

    @Override
    protected void afterRouteCalling(Exchange exchange) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        exchange.getMessage().setBody(extractServiceParametersResponseBody(originalMessage, exchange.getMessage().getBody()));
    }

    protected abstract void call(TryDefinition tryDefinition);

    @Override
    protected List<CamelInvocationStep> callRoute(TryDefinition routeDefinition) {
       List<CamelInvocationStep> steps = new ArrayList<>();
       steps.add(this::call);
       return steps;
    }


    protected abstract MessageOutput buildMessageOutput();

    protected abstract Object extractServiceParametersResponseBody(Message message, Object body) ;

    protected abstract Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput);

    protected Optional<Object> extractParameterValue(Message message, Parameter parameter) {
        return ParameterDataProvider.getInstance().extractParameterValue(message, parameter);
    }

    private Object extractRequestBody(Exchange exchange, MessageOutput messageOutput) {
        Object body = exchange.getMessage().getBody();
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        AbstractExternalService<?> service = (AbstractExternalService<?>) originalMessage.getHeader().getService();
        if (Objects.nonNull(service.getRequestBodyType())) {
            return switch (service.getRequestBodyType()) {
                case NONE -> null;
                case MESSAGE_BODY -> body;
                case PARAMETERS -> extractServiceParametersRequestBody(originalMessage, body, messageOutput);
            };
        }
        return body;
    }

}
