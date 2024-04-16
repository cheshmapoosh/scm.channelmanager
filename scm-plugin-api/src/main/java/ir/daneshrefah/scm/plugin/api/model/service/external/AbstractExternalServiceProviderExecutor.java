package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.OutboundEvent;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.MessageUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
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
public abstract class AbstractExternalServiceProviderExecutor implements ExternalServiceProviderExecutor {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected static final String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";
    protected static final String HEADER_START_TIME = "ScmProviderStartTime";
    protected static final String HEADER_END_TIME = "ScmProviderEndTime";
    protected static final String HEADER_REQUEST_BODY = "ScmRequestBody";
    protected static final String HEADER_RESPONSE_BODY = "ScmResponseBody";
    protected static final String HEADER_TARGET_URL = "ScmTargetUrl";

    //    private final ServiceService serviceService;
    private final ProducerTemplate producerTemplate;
    private final CamelContext camelContext;
    private final ResourceService resourceService;
    protected final ObjectMapper objectMapper;
    @Getter(AccessLevel.PROTECTED)
    @Setter
    private ExternalServiceProvider provider;


    public final void configureRouteDefinition(RouteDefinition routeDefinition, ExternalServiceProvider provider) {
        this.provider = provider;
        routeDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            exchange.setProperty(HEADER_ORIGINAL_MESSAGE, message);
            JsonNode requestBody = transformRequest(message);
            exchange.getMessage().setBody(requestBody);
            exchange.setProperty(HEADER_REQUEST_BODY, requestBody);
            exchange.setProperty(HEADER_START_TIME, Instant.now());
        });
        invokeTargetEndpoint(routeDefinition);
        routeDefinition.process(exchange -> {
            exchange.setProperty(HEADER_END_TIME, Instant.now());
            String response = exchange.getMessage().getBody(String.class);
            exchange.setProperty(HEADER_RESPONSE_BODY, response);
            JsonNode jsonResponse = null;
            try {
                jsonResponse = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                jsonResponse = objectMapper.valueToTree(response);
            }
            if (null != exchange.getException()) {
                return;
            }
            Message message = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
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
        logOutboundEvent(exchange);
        Exception exception = exchange.getException();
        if (null != exception) {
            throw new RuntimeException(exception);
        }
        Message responseMessage = exchange.getMessage().getBody(Message.class);
        return responseMessage.getPayload();
    }

    protected String extractProviderEndpoint() {
        if (null == provider || null == provider.getMetadata() || StringUtils.isEmpty(provider.getMetadata().getEndpoint())) {
            return null;
        }
        String result = resourceService.prepareProperties(provider.getMetadata().getEndpoint());
        return StringUtils.appendIfMissing(result, "/");
    }

    private void logOutboundEvent(Exchange exchange) {
        Message message = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
        Instant startTime = exchange.getProperty(HEADER_START_TIME, Instant.class);
        Instant endTime = exchange.getProperty(HEADER_END_TIME, Instant.class);
        endTime = null != endTime ? endTime : Instant.now();
        String username = MessageUtils.getUsername(message);
        String cspUsername = MessageUtils.getCSPUsername(message);
        String requestBody = exchange.getProperty(HEADER_REQUEST_BODY, String.class);
        String responseBody = exchange.getProperty(HEADER_RESPONSE_BODY, String.class);
        String targetUrl = exchange.getMessage().getHeader(HEADER_TARGET_URL, String.class);
        Event event = OutboundEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .terminalCode(message.getHeader().getTerminalCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .username(username)
                .cspUsername(cspUsername)
                .error(exchange.getException())
                .exceptionClassName(null != exchange.getException() ? exchange.getException().getClass().getName() : null)
                .threadName(Thread.currentThread().getName())
                .startTime(startTime)
                .providerCode(provider.getCode())
                .providerTargetUrl(targetUrl)
                .providerResponseCode(null)
                .request(requestBody)
                .response(responseBody)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    private JsonNode transformRequest(Message message) {
        JsonNode requestBody = message.getPayload();
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

    public abstract void invokeTargetEndpoint(RouteDefinition routeDefinition);

}
