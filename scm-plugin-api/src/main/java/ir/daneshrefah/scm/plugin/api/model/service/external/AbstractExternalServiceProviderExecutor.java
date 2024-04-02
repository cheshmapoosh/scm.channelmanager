package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-30
 */
@RequiredArgsConstructor
public abstract class AbstractExternalServiceProviderExecutor extends RouteBuilder implements ExternalServiceProviderExecutor {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected static final String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";
    protected static final String HEADER_START_TIME = "ScmProviderStartTime";
    protected static final String HEADER_END_TIME = "ScmProviderEndTime";

    private final ServiceService serviceService;
    private final ProducerTemplate producerTemplate;
    private final CamelContext camelContext;
    protected final ObjectMapper objectMapper;
    @Getter
    private ExternalServiceProvider provider;

    @Override
    public final void configure() throws Exception {
        String providerCode = extractProviderCode();
        provider = serviceService.findServiceProviderByCode(providerCode);
        String fromUri = "ESP_" + provider.getCode();
        RouteDefinition routeDefinition = from("direct:" + fromUri).routeId("ESP_" + fromUri);
        routeDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            exchange.getMessage().setHeader(HEADER_ORIGINAL_MESSAGE, message);
            exchange.getMessage().setBody(transformRequest(message));
            exchange.getMessage().setHeader(HEADER_START_TIME, Instant.now());
        });
        invokeTargetEndpoint(routeDefinition);
        routeDefinition.process(exchange -> {
            exchange.getMessage().setHeader(HEADER_END_TIME, Instant.now());
            String response = exchange.getMessage().getBody(String.class);
            JsonNode jsonResponse = null;
            try {
                jsonResponse = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                jsonResponse = objectMapper.valueToTree(response);
            }
            if (null != exchange.getException()) {
                return;
            }
            Message message = exchange.getMessage().getHeader(HEADER_ORIGINAL_MESSAGE, Message.class);
            exchange.getMessage().removeHeader(HEADER_ORIGINAL_MESSAGE);
            message.payload(transformResponse(message, jsonResponse));
            exchange.getMessage().setBody(message);
        });
        routeDefinition.end();
    }

    @Override
    public final JsonNode execute(Message message, Service service) {
        Exchange exchange = new DefaultExchange(camelContext);
        String targetEndpoint = "direct:ESP_" + provider.getCode();
        exchange.getMessage().setBody(message);
        exchange = producerTemplate.send(targetEndpoint, exchange);
        Exception exception = exchange.getException();
        if (null != exception) {
            throw new RuntimeException(exception);
        }
        Message responseMessage = exchange.getMessage().getBody(Message.class);
        return transformResponse(message, responseMessage.getPayload());
    }

    private Object transformRequest(Message message) {
        Object requestBody = message.getPayload();
        List<AbstractTransformer> requestTransformers = prepareRequestTransformers();
        for (Iterator<AbstractTransformer> iterator = requestTransformers.iterator(); iterator.hasNext(); ) {
            AbstractTransformer transformer = iterator.next();
            requestBody = transformer.transform(requestBody, message, message.getHeader().getServiceAccess().getService().getMetadata());
        }
        return requestBody;
    }

    private JsonNode transformResponse(Message message, JsonNode response) {
        List<AbstractTransformer> responseTransformers = prepareResponseTransformers();
        for (Iterator<AbstractTransformer> iterator = responseTransformers.iterator(); iterator.hasNext(); ) {
            AbstractTransformer transformer = iterator.next();
            response = transformer.transform(response, message, message.getHeader().getServiceAccess().getService().getMetadata());
        }
        return response;
    }

    protected List<AbstractTransformer> prepareRequestTransformers() {
        return Collections.emptyList();
    }

    protected List<AbstractTransformer> prepareResponseTransformers() {
        return Collections.emptyList();
    }

    public abstract String extractProviderCode();

    protected abstract void invokeTargetEndpoint(RouteDefinition routeDefinition);

}
