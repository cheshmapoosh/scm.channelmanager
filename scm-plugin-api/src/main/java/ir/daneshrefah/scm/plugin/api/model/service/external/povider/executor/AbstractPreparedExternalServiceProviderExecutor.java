package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractAuditableExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor.helper.ParameterBodyConsumer;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import org.apache.camel.Exchange;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractPreparedExternalServiceProviderExecutor extends AbstractBaseExternalServiceProviderExecutor{

    public AbstractPreparedExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    public final Object extractBody(Exchange exchange, MessageOutput messageOutput, ParameterBodyConsumer parameterBodyConsumer) {
        Object body = exchange.getMessage().getBody();
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        AbstractAuditableExternalService<?> service = (AbstractAuditableExternalService<?>) originalMessage.getHeader().getService();
        if (Objects.nonNull(service.getRequestBodyType())) {
            return switch (service.getRequestBodyType()) {
                case NONE -> null;
                case MESSAGE_BODY -> body;
                case PARAMETERS -> parameterBodyConsumer.apply(originalMessage, body, messageOutput);
            };
        }
        return body;
    }

    protected Optional<Object> extractParameterValue(Message message, Parameter parameter) {
        return ParameterDataProvider.getInstance().extractParameterValue(message, parameter);
    }

    protected void setupMessageOutput(Exchange exchange,MessageOutput messageOutput,Object body) {
        Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        messageOutput.setExternalCorrelationId(getProviderCorrelationId(originalMessage));
        messageOutput.setBody(body);
        exchange.getMessage().setBody(messageOutput.getBody());
        exchange.setProperty(HEADER_MESSAGE_OUTPUT, messageOutput);
    }

}
