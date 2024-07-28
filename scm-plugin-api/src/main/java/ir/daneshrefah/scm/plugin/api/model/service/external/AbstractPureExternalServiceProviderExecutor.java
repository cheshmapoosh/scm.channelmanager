package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
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
public abstract class AbstractPureExternalServiceProviderExecutor extends AbstractExternalServiceProviderExecutor {

    public AbstractPureExternalServiceProviderExecutor(ResourceService resourceService, ServiceService serviceService, ObjectMapper objectMapper) {
        super(resourceService, serviceService, objectMapper);
    }

    @Override
    public final void intiEndpointCallRouteDefinitionInternal(TryDefinition routeDefinition) {
        routeDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            Object body = exchange.getMessage().getBody();
            JsonNode response = executeEndpoint(originalMessage, body);
            exchange.getMessage().setBody(response);
        });
    }

    public abstract JsonNode executeEndpoint(Message originalMessage, Object body);

}
