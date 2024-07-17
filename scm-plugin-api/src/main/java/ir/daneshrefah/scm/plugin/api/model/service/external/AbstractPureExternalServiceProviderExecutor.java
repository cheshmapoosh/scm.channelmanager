package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import org.apache.camel.model.RouteDefinition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
public abstract class AbstractPureExternalServiceProviderExecutor extends AbstractExternalServiceProviderExecutor {

    public AbstractPureExternalServiceProviderExecutor(ResourceService resourceService, ObjectMapper objectMapper) {
        super(resourceService, objectMapper);
    }

    @Override
    public final void intiEndpointCallRouteDefinitionInternal(RouteDefinition routeDefinition) {
        routeDefinition.process(exchange -> {
            Message originalMessage = exchange.getMessage().getHeader(HEADER_ORIGINAL_MESSAGE, Message.class);
            Object body = exchange.getMessage().getBody();
            JsonNode response = executeEndpoint(originalMessage, body);
            exchange.getMessage().setBody(response);
        });
    }

    public abstract JsonNode executeEndpoint(Message originalMessage, Object body);

}
