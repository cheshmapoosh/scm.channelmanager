package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ResourceService;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
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

    public AbstractCamelExternalServiceProviderExecutor(ProducerTemplate producerTemplate, CamelContext camelContext,
                                                        ResourceService resourceService, ObjectMapper objectMapper) {
        super(producerTemplate, camelContext, resourceService, objectMapper);
    }

    @Override
    public final void invokeTargetEndpoint(RouteDefinition routeDefinition) {
        routeDefinition.process(exchange -> {
            Message originalMessage = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            Object body = exchange.getMessage().getBody();
            exchange.getMessage().setHeader(HEADER_TARGET_URL, extractTargetUrl(originalMessage));
            Map<String, Object> headers = obtainRequestHeaders(originalMessage);
            if (null != headers && !headers.isEmpty()) {
                for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext(); ) {
                    String header = iterator.next();
                    exchange.getMessage().setHeader(header, headers.get(header));
                }
            }

        });
        routeDefinition.toD("${header." + HEADER_TARGET_URL + "}");
    }

    protected Map<String, Object> obtainRequestHeaders(Message message) {
        return Collections.emptyMap();
    }

    protected abstract String extractTargetUrl(Message message);
}
