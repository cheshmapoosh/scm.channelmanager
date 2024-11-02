package ir.daneshrefah.scm.plugin.api.model.service.external.povider.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageOutput;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.common.service.ServiceService;
import org.apache.camel.model.TryDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
public abstract class AbstractPureExternalServiceProviderExecutor extends AbstractSingleStepExternalServiceProviderExecutor {


    public AbstractPureExternalServiceProviderExecutor(ObjectMapper objectMapper, ResourceService resourceService, ServiceService serviceService) {
        super(objectMapper, resourceService, serviceService);
    }

    @Override
    public void call(TryDefinition tryDefinition) {
        tryDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            Object body = exchange.getMessage().getBody();
            JsonNode response = (JsonNode) extractServiceParametersResponseBody(originalMessage,body);
            exchange.getMessage().setBody(response);
        });
    }


    @Override
    protected Object extractServiceParametersResponseBody(Message message, Object body) {
        return executeEndpoint(message, body);
    }

    @Override
    protected Object extractServiceParametersRequestBody(Message message, Object body, MessageOutput messageOutput) {
        return message;
    }

    public abstract JsonNode executeEndpoint(Message originalMessage, Object body);

}
