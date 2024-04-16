package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.ServiceEvent;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.utils.MessageUtils;
import org.apache.camel.Exchange;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.TryDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
public abstract class ServiceExecutor {

    protected static final String PROPERTY_START_TIME = "ScmServiceStartTime";
    protected static final String PROPERTY_END_TIME = "ScmServiceEndTime";
    protected static final String PROPERTY_REQUEST_BODY = "ScmServiceRequestBody";

    @Autowired
    protected ErrorHandlerService errorHandlerService;
    @Autowired
    protected ObjectMapper objectMapper;
    private List<MessageInterceptor> requestInterceptors;
    private List<MessageInterceptor> responseInterceptors;

    public final void init(RouteBuilderDelegator routeBuilder, List<MessageInterceptor> requestInterceptors, List<MessageInterceptor> responseInterceptors) {
        this.requestInterceptors = requestInterceptors;
        this.responseInterceptors = responseInterceptors;
        initConfigs(routeBuilder);
    }

    protected void initConfigs(RouteBuilderDelegator routeBuilder) {
        // can override in child class for additional configs
    }

    public JsonNode transformRequest(List<TransformerExecutionWrapper> transformerRelations, Message message) {
        JsonNode payload = message.getPayload();
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        return payload;
    }

    public JsonNode transformResponse(List<TransformerExecutionWrapper> transformerRelations, Message message, JsonNode payload) {
        for (Iterator<TransformerExecutionWrapper> iterator = transformerRelations.iterator(); iterator.hasNext(); ) {
            TransformerExecutionWrapper transformerExecutionWrapper = iterator.next();
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());
        }
        return payload;
    }

    private void logServiceCallEvent(Message message, Service service, Object input, Exception exception, Instant startTime) {
        Instant endTime = Instant.now();
        String username = MessageUtils.getUsername(message);
        String cspUsername = MessageUtils.getCSPUsername(message);
        String requestBody = null != input ? input.toString() : null;
        String responseBody = null != message.getPayload() ? message.getPayload().toString() : null;
        Event event = ServiceEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .terminalCode(message.getHeader().getTerminalCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .username(username)
                .cspUsername(cspUsername)
                .error(exception)
                .exceptionClassName(null != exception ? exception.getClass().getName() : null)
                .threadName(Thread.currentThread().getName())
                .startTime(startTime)
                .serviceCode(service.getCode())
                .request(requestBody)
                .response(responseBody)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .status(message.getStatus())
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

    public final void initServiceExecution(Service service, OutputDefinition routeDefinition) {
        TryDefinition tryDefinition = routeDefinition.doTry();
        tryDefinition = tryDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            exchange.setProperty(PROPERTY_START_TIME, Instant.now());
            exchange.setProperty(PROPERTY_REQUEST_BODY, message.getPayload().deepCopy());
            for (Iterator<MessageInterceptor> iterator = requestInterceptors.iterator(); iterator.hasNext(); ) {
                MessageInterceptor messageInterceptor = iterator.next();
                message = messageInterceptor.intercept(message);
                if (!MessageUtils.isContinueAllowed(message)) {
                    break;
                }
            }
        });
        ChoiceDefinition choiceDefinition = tryDefinition.choice()
                .when(exchange -> {
                    boolean isContinueAllowed = MessageUtils.isContinueAllowed(exchange.getMessage().getBody(Message.class));
                    return isContinueAllowed;
                });
        defineServiceRoute(service, choiceDefinition);
        choiceDefinition.endChoice();
        tryDefinition = tryDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            if (!MessageUtils.isContinueAllowed(message)) {
                exchange.setProperty(PROPERTY_END_TIME, Instant.now());
                return;
            }
            for (Iterator<MessageInterceptor> iterator = responseInterceptors.iterator(); iterator.hasNext(); ) {
                MessageInterceptor messageInterceptor = iterator.next();
                message = messageInterceptor.intercept(message);
                if (!MessageUtils.isContinueAllowed(message)) {
                    return;
                }
            }
            exchange.setProperty(PROPERTY_END_TIME, Instant.now());
            if (MessageStatus.SC_PROCESSING.equals(message.getStatus())) {
                message.status(MessageStatus.SC_SUCCESS);
            }
        });
        tryDefinition = tryDefinition.doCatch(Exception.class);
        tryDefinition.process(exchange -> {
            Exception exception = extractException(exchange);
            Message message = exchange.getMessage().getBody(Message.class);
            errorHandlerService.resolveMessageByException(message, exception);
            exchange.getMessage().setBody(message);
        });
        tryDefinition = tryDefinition.doFinally();
        tryDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            Exception exception = extractException(exchange);
            Instant startTime = exchange.getProperty(PROPERTY_START_TIME, Instant.class);
            JsonNode request = exchange.getProperty(PROPERTY_REQUEST_BODY, JsonNode.class);
            logServiceCallEvent(message, service, request, exception, startTime);
        });
        tryDefinition.end();
    }

    private Exception extractException(Exchange exchange) {
        Exception exception = exchange.getException();
        if (null == exception) {
            exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }
        if (null == exception) {
            exception = exchange.getProperty(Exchange.EXCEPTION_HANDLED, Exception.class);
        }
        return exception;
    }

    protected abstract void defineServiceRoute(Service service, ProcessorDefinition processorDefinition);

}
