package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import org.apache.camel.model.RouteDefinition;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractCamelExternalServiceProviderExecutor extends AbstractExternalServiceProviderExecutor {


    public AbstractCamelExternalServiceProviderExecutor(ResourceService resourceService, ObjectMapper objectMapper) {
        super(resourceService, objectMapper);
    }

    @Override
    public final void intiEndpointCallRouteDefinitionInternal(RouteDefinition routeDefinition) {
        routeDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            exchange.getMessage().setHeader(HEADER_TARGET_URL, extractTargetEndpointUrl(originalMessage));
            Map<String, Object> headers = extractRequestHeaders(originalMessage);
            if (null != headers && !headers.isEmpty()) {
                for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext(); ) {
                    String header = iterator.next();
                    exchange.getMessage().setHeader(header, headers.get(header));
                }
            }

        });
        routeDefinition.toD("${header." + HEADER_TARGET_URL + "}");
    }

    protected Map<String, Object> extractRequestHeaders(Message message) {
        return Collections.emptyMap();
    }

    protected abstract String extractTargetEndpointUrl(Message message);
}
