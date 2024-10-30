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
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.*;
import org.apache.camel.spi.ErrorHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
public abstract class ServiceExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExecutor.class);

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

    public final void init(RouteBuilder routeBuilder, List<MessageInterceptor> requestInterceptors, List<MessageInterceptor> responseInterceptors) {
        this.requestInterceptors = requestInterceptors;
        this.responseInterceptors = responseInterceptors;
        initConfigs(routeBuilder);
    }

    protected void initConfigs(RouteBuilder routeBuilder) {
        AtomicReference<MessageInterceptor> nextMessageInterceptorRef = new AtomicReference<>(null);
        if (CollectionUtils.isNotEmpty(requestInterceptors)) {
            routeBuilder.interceptSendToEndpoint("log:request-interceptors")
                    .to("direct:REQ_INTERCEPTOR_" + requestInterceptors.get(0).getClass().getSimpleName())
                    .skipSendToOriginalEndpoint()
                    .when(exchange -> {
                        Message message = exchange.getMessage().getBody(Message.class);
                        return !message.isContinueAllowed();
                    });

            final int size = requestInterceptors.size();
            IntStream.rangeClosed(1, size)
                    .mapToObj(index -> responseInterceptors.get(size - index))
                    .forEach(messageInterceptor -> {
                        RouteDefinition routeDefinition = routeBuilder.from("direct:REQ_INTERCEPTOR_" + messageInterceptor.getClass().getSimpleName());

                        MessageInterceptor nextMessageInterceptor = nextMessageInterceptorRef.get();
                        if (nextMessageInterceptor != null) {
                            routeDefinition
                                    .process(exchange -> {
                                        Message message = exchange.getMessage().getBody(Message.class);
                                        nextMessageInterceptor.intercept(message);
                                    })
                                    .choice()
                                    .when(exchange -> {
                                        Message message = exchange.getMessage().getBody(Message.class);
                                        return message.isContinueAllowed();
                                    })
                                    .to("direct:REQ_INTERCEPTOR_" + nextMessageInterceptor.getClass().getSimpleName())
                                    .endChoice();
                        }
                        nextMessageInterceptorRef.set(messageInterceptor);
                    });
        }

        nextMessageInterceptorRef.set(null);

        if (CollectionUtils.isNotEmpty(responseInterceptors)) {
            routeBuilder.interceptSendToEndpoint("log:response-interceptors")
                    .to("direct:RES_INTERCEPTOR_" + responseInterceptors.get(0).getClass().getSimpleName())
                    .skipSendToOriginalEndpoint()
                    .when(exchange -> {
                        Message message = exchange.getMessage().getBody(Message.class);
                        return message.isContinueAllowed();
                    });

            final int size = responseInterceptors.size();
            IntStream.rangeClosed(1, size)
                    .mapToObj(index -> responseInterceptors.get(size - index))
                    .forEach(messageInterceptor -> {
                        RouteDefinition routeDefinition = routeBuilder.from("direct:RES_INTERCEPTOR_" + messageInterceptor.getClass().getSimpleName());
                        MessageInterceptor nextMessageInterceptor = nextMessageInterceptorRef.get();
                        if (nextMessageInterceptor != null) {
                            routeDefinition
                                    .process(exchange -> {
                                        Message message = exchange.getMessage().getBody(Message.class);
                                        nextMessageInterceptor.intercept(message);
                                    })
                                    .choice()
                                    .when(exchange -> {
                                        Message message = exchange.getMessage().getBody(Message.class);
                                        return message.isContinueAllowed();
                                    })
                                    .to("direct:RES_INTERCEPTOR_" + nextMessageInterceptor.getClass().getSimpleName())
                                    .endChoice();
                        }
                        nextMessageInterceptorRef.set(messageInterceptor);
                    });
        }
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


    public final void initServiceExecution(Service service, RouteBuilder routeBuilder) {
        String serviceCode = service.getCode();
        // For created dynamic proxy service , the route created by $_proxy ... name , but the service code set as same as
        // target service.
        if (service.isProxy()) {
            serviceCode = service.getTargetProxyCode();
        }
        String fromUri = "SVI_" + serviceCode;
        LOGGER.info("start define service '{}' with uri '{}'", service.getId(), fromUri);
        RouteDefinition routeDefinition = routeBuilder.from("direct:" + fromUri).routeId("SERVICE_" + fromUri);

        TryDefinition tryDefinition = routeDefinition.doTry();
        tryDefinition = tryDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            exchange.setProperty(PROPERTY_START_TIME, Instant.now());
            exchange.setProperty(PROPERTY_REQUEST_BODY, message.getPayload().deepCopy());
        });

//        Request Interceptors
//        tryDefinition.setProperty("index", () -> 0)
//                .loopDoWhile(exchange -> {
//                    Message message = exchange.getMessage().getBody(Message.class);
//                    Integer index = exchange.getProperty("index", Integer.class);
//                    index++;
//                    exchange.setProperty("index", index);
//                    return message.isContinueAllowed() && index < requestInterceptors.size();
//                }).process(exchange -> {
//                    Message message = exchange.getMessage().getBody(Message.class);
//                    Integer counter = exchange.getProperty("index", Integer.class);
//                    requestInterceptors.get(counter).intercept(message);
//                })
//                .end()
//                .removeProperty("index");
        tryDefinition.to("log:request-interceptors");

        ChoiceDefinition choiceDefinition = tryDefinition.choice()
                .when(exchange -> exchange.getMessage().getBody(Message.class).isContinueAllowed());
        defineServiceRoute(service, choiceDefinition);
        choiceDefinition.endChoice();

//        Response Interceptors
//        tryDefinition.setProperty("index", () -> 0)
//                .loopDoWhile(exchange -> {
//                    Message message = exchange.getMessage().getBody(Message.class);
//                    Integer index = exchange.getProperty("index", Integer.class);
//                    index++;
//                    exchange.setProperty("index", index);
//                    return message.isContinueAllowed() && index < responseInterceptors.size();
//                }).process(exchange -> {
//                    Message message = exchange.getMessage().getBody(Message.class);
//                    Integer counter = exchange.getProperty("index", Integer.class);
//                    responseInterceptors.get(counter).intercept(message);
//                })
//                .end()
//                .removeProperty("index");

        tryDefinition.to("log:response-interceptors");

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

    public static void main(String[] args) throws Exception {
        // Setup the main Camel context
        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

//                // First global intercept (applies to all routes)
//                intercept()
//                        .to("log:firstIntercept")
//                        .process(exchange -> {
//                            // Additional processing or logging
//                            System.out.println("First global intercept executed");
//                        });
//
//                // Second global intercept
//                intercept()
//                        .to("log:secondIntercept")
//                        .process(exchange -> {
//                            // Additional processing or logging
//                            System.out.println("Second global intercept executed");
//                        });

                // Specific endpoint intercept

//                interceptSendToEndpoint("direct:endpointA")
//                        .to("log:endpointIntercept1")
//                        .process(exchange -> {
//                            // Processing logic for first endpoint intercept
//                            System.out.println("Endpoint intercept 1 executed");
//                        });

                interceptSendToEndpoint("log:endpointA")
//                        .to("direct:intercept1")
                        .skipSendToOriginalEndpoint()
                        .when(exchange -> true)
                        .to("log:skip")
                        .process(exchange -> {
                            // Custom logic
                            System.out.println("Intercepted and stopped message for ServiceA");
                            // You could also set a custom response here if needed
                            exchange.getMessage().setBody("Intercepted message; stopping further processing.");
                        }).afterUri("log:afterUri");

                ;


                from("direct:intercept1")
                        .log("intercept1 run endpointA")
                        .choice().when(exchange -> true).to("direct:intercept2").otherwise().log("Skip direct:intercept2").endChoice();

                from("direct:intercept2")
                        .log("intercept2 run endpointA");

                // Main route definition
                from("timer://start?repeatCount=1&delay=1000")
                        .doTry()
                        .to("log:endpointA")
                        .to("log:end")
                        .doFinally().to("log:finally")
                        .endDoTry();

//                from("direct:endpointA")
//                        .log("Successful run endpointA");

            }
        });

        context.start();
        Thread.sleep(5000);
        context.stop();
    }

}
