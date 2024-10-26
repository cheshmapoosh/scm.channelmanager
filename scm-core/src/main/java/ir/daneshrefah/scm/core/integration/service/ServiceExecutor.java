package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.logging.utils.TraceLogUtils;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.transformer.TransformerExecutionWrapper;
import ir.daneshrefah.scm.utils.MessageInputContext;
import org.apache.camel.Exchange;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.TryDefinition;
import org.apache.camel.spi.ErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Autowired
    protected TraceLogUtils traceLogUtils;
    @Autowired
    private Tracer tracer;
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

    protected final List<TransformerExecutionWrapper> prepareTransformerExecutionWrapper(List<TransformerRelation> transformerRelations,
                                                                                         TransformerRelationType filter) {
        if (Objects.isNull(transformerRelations) || transformerRelations.isEmpty()) {
            return Collections.emptyList();
        }
        return transformerRelations.stream()
                .filter(t -> null == filter || filter.equals(t.getRelationType()))
                .map(TransformerExecutionWrapper::new)
                .collect(Collectors.toList());
    }

    public final JsonNode transformRequest(List<TransformerExecutionWrapper> transformerRelations, Message message) {
        JsonNode payload = message.getPayload();
        if (Objects.isNull(transformerRelations) || transformerRelations.isEmpty()) {
            return payload;
        }
        for (TransformerExecutionWrapper transformerExecutionWrapper : transformerRelations) {
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());

        }
        return payload;
    }

    public JsonNode transformResponse(List<TransformerExecutionWrapper> transformerRelations, Message message, JsonNode payload) {
        if (Objects.isNull(transformerRelations) || transformerRelations.isEmpty()) {
            return payload;
        }
        for (TransformerExecutionWrapper transformerExecutionWrapper : transformerRelations) {
            payload = transformerExecutionWrapper.getTransformerInstance()
                    .transform(payload, message, transformerExecutionWrapper.getTransformerRelation().getMetadata());
        }
        return payload;
    }


    public final void initServiceExecution(Service service, OutputDefinition<?> routeDefinition) {
        TryDefinition tryDefinition = routeDefinition.doTry();
        tryDefinition = tryDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            exchange.setProperty(PROPERTY_START_TIME, Instant.now());
            exchange.setProperty(PROPERTY_REQUEST_BODY, message.getPayload().deepCopy());
            for (MessageInterceptor messageInterceptor : requestInterceptors) {
                message = messageInterceptor.intercept(message);
                if (!message.isContinueAllowed()) {
                    break;
                }
            }
        });
        ChoiceDefinition choiceDefinition = tryDefinition.choice()
                .when(exchange -> exchange.getMessage().getBody(Message.class).isContinueAllowed());
        defineServiceRoute(service, choiceDefinition);
        choiceDefinition.endChoice();
        tryDefinition = tryDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            if (!message.isContinueAllowed()) {
                exchange.setProperty(PROPERTY_END_TIME, Instant.now());
                return;
            }
            for (MessageInterceptor messageInterceptor : responseInterceptors) {
                message = messageInterceptor.intercept(message);
                if (!message.isContinueAllowed()) {
                    return;
                }
            }
            exchange.setProperty(PROPERTY_END_TIME, Instant.now());
            if (MessageStatus.SC_PROCESSING.equals(message.getStatus())) {
                message.status(MessageStatus.SC_SUCCESS);
            }
        });
        tryDefinition.doCatch(Exception.class)
                .process((ErrorHandler) exchange -> {
                    Exception exception = extractException(exchange);
                    Message message = exchange.getMessage().getBody(Message.class);
                    errorHandlerService.resolveMessageByException(message, exception);
                    exchange.getMessage().setBody(message);
                })
                .doFinally()
                .process(exchange -> {
                    Message message = exchange.getMessage().getBody(Message.class);
                    Exception exception = extractException(exchange);
                    Instant startTime = exchange.getProperty(PROPERTY_START_TIME, Instant.class);
                    JsonNode request = exchange.getProperty(PROPERTY_REQUEST_BODY, JsonNode.class);
                    MessageInput<?> messageInput = MessageInputContext.getCurrentContext();
                    Span span = tracer.spanBuilder(messageInput.getServiceCode()).setSpanKind(SpanKind.SERVER).startSpan();
                    try {
                        traceLogUtils.recordMessageTrace(message, request, exception, span);
                        span.makeCurrent();
                    } catch (Exception ex) {
                        span.end();
                    }
                })
                .end();
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

    protected abstract void defineServiceRoute(Service service, ProcessorDefinition<?> processorDefinition);

}
