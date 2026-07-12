package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.ObservationIds;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.TraceContext;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.starter.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class CoreObservationTraceSupport {
    public static final String GATEWAY_SCOPE_PROPERTY = "scm.observation.scope.gateway";
    public static final String SERVICE_SCOPE_PROPERTY = "scm.observation.scope.service";
    public static final String OPERATION_SCOPE_PROPERTY = "scm.observation.scope.operation";

    public static final String GATEWAY_CONTEXT_PROPERTY = "scm.observation.context.gateway";
    public static final String SERVICE_CONTEXT_PROPERTY = "scm.observation.context.service";
    public static final String OPERATION_CONTEXT_PROPERTY = "scm.observation.context.operation";

    private static final String BUSINESS_FAILURE_PROPERTY = "scm.observation.business.failure";
    private static final String GATEWAY_RESPONSE_EVENT_PROPERTY = "scm.observation.gateway.response.event.recorded";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

    private final ObjectProvider<ScmObservation> observationProvider;
    private final ObjectProvider<GatewayAuthenticationTraceEnricher> authenticationTraceEnrichers;
    private final ScmExchangeMdc exchangeMdc;

    CoreObservationTraceSupport(
            ObjectProvider<ScmObservation> observationProvider,
            ObjectProvider<GatewayAuthenticationTraceEnricher> authenticationTraceEnrichers,
            ScmExchangeMdc exchangeMdc
    ) {
        this.observationProvider = observationProvider;
        this.authenticationTraceEnrichers = authenticationTraceEnrichers;
        this.exchangeMdc = exchangeMdc;
    }

    public void startGatewayReceive(Exchange exchange, Service service) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null || exchange == null || exchange.getProperty(GATEWAY_SCOPE_PROPERTY) != null) {
            return;
        }

        GatewayObservationContext preparedContext = preparedGatewayContext(exchange);
        TraceContext currentContext = existingContext(exchange);
        String traceId = firstText(
                preparedContext == null ? null : preparedContext.traceId(),
                property(exchange, Message.TRACE_ID),
                currentContext == null ? null : currentContext.traceId(),
                ObservationIds.traceId()
        );
        String spanId = firstText(
                preparedContext == null ? null : preparedContext.gatewaySpanId(),
                ObservationIds.spanId()
        );
        String correlationId = firstText(
                preparedContext == null ? null : preparedContext.correlationId(),
                correlationId(exchange),
                currentContext == null ? null : currentContext.correlationId(),
                ObservationIds.correlationId()
        );
        TraceContext requestedContext = new TraceContext(
                traceId,
                spanId,
                correlationId,
                CorrelationType.REQUEST.value()
        );

        Map<String, String> fields = exchangeMdc.fields(exchange);
        HttpServletRequest servletRequest = servletRequest(exchange);
        ObservationScope scope = observation.trace()
                .span("gateway.receive")
                .spanKind("server")
                .action("gateway.receive")
                .traceId(requestedContext.traceId())
                .spanId(requestedContext.spanId())
                .parentSpanId(preparedContext == null ? null : preparedContext.remoteParentSpanId())
                .traceFlags(preparedContext == null ? null : preparedContext.traceFlags())
                .correlationId(requestedContext.correlationId())
                .correlationType(requestedContext.correlationType())
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, firstText(
                        preparedContext == null ? null : preparedContext.gatewayName(),
                        fields.get("gatewayName")))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, firstText(
                        preparedContext == null ? null : preparedContext.channelCode(),
                        fields.get("channelCode")))
                .attribute(CommonTraceAttributes.SCM_PROTOCOL, firstText(
                        preparedContext == null ? null : preparedContext.protocol(),
                        RouteLogSupport.protocol(exchange)))
                .attribute(CommonTraceAttributes.SCM_REQUEST_NAME, firstText(
                        preparedContext == null ? null : preparedContext.requestName(),
                        requestName(servletRequest)))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, exchange.getFromRouteId())
                .attribute(CoreTraceAttributes.EXCHANGE_ID, exchange.getExchangeId())
                .attribute(CoreTraceAttributes.SERVICE_CODE, service == null ? fields.get("serviceCode") : service.getCode())
                .attribute(CoreTraceAttributes.SERVICE_NAME, service == null ? null : service.getName())
                .attribute(CoreTraceAttributes.SERVICE_VERSION, fields.get("serviceVersion"))
                .attribute(CommonTraceAttributes.HTTP_METHOD, httpMethod(exchange, servletRequest))
                .attribute(CommonTraceAttributes.URL_PATH, requestPath(exchange, servletRequest))
                .attribute(CommonTraceAttributes.HTTP_QUERY_PRESENT, hasQuery(exchange, servletRequest))
                .attribute(CommonTraceAttributes.CLIENT_IP, clientIp(servletRequest))
                .attribute(CommonTraceAttributes.CLIENT_ADDRESS, stringAttribute(
                        servletRequest, CommonTraceAttributes.CLIENT_ADDRESS.name()))
                .attribute("scm.observation.legacy.enabled", requestAttribute(
                        servletRequest, "scm.observation.legacy.enabled"))
                .attribute("scm.observation.legacy.service.code", requestAttribute(
                        servletRequest, "scm.observation.legacy.service.code"))
                .attribute("scm.observation.legacy.operation.code", requestAttribute(
                        servletRequest, "scm.observation.legacy.operation.code"))
                .startDetached();
        TraceContext gatewayContext = startedContext(scope, requestedContext);
        putContext(exchange, GATEWAY_CONTEXT_PROPERTY, gatewayContext);
        exchange.setProperty(GATEWAY_SCOPE_PROPERTY, scope);
        enrichGatewayAuthentication(exchange, SecurityContextHolder.getContext().getAuthentication());
    }

    public void gatewayResponseCompleted(Exchange exchange, boolean scmFault) {
        if (exchange == null) {
            return;
        }
        Integer statusCode = httpStatus(exchange);
        boolean failure = scmFault || statusCode != null && statusCode >= 400;
        if (failure) {
            exchange.setProperty(BUSINESS_FAILURE_PROPERTY, Boolean.TRUE);
        }

        ObservationScope gatewayScope = activeScope(exchange, "gateway");
        if (gatewayScope == null
                || Boolean.TRUE.equals(exchange.getProperty(GATEWAY_RESPONSE_EVENT_PROPERTY, Boolean.class))) {
            return;
        }
        exchange.setProperty(GATEWAY_RESPONSE_EVENT_PROPERTY, Boolean.TRUE);
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(CommonTraceAttributes.EVENT_OUTCOME.name(), failure ? OUTCOME_FAILURE : OUTCOME_SUCCESS);
        if (statusCode != null) {
            attributes.put(CommonTraceAttributes.HTTP_STATUS_CODE.name(), statusCode);
        }
        gatewayScope.event("gateway.response.completed", attributes);
    }

    public void finishGatewayReceive(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        Throwable failure = exchangeFailure(exchange);
        Integer statusCode = httpStatus(exchange);
        boolean failed = hasBusinessFailure(exchange, failure);
        exchange.removeProperty(BUSINESS_FAILURE_PROPERTY);
        exchange.removeProperty(GATEWAY_RESPONSE_EVENT_PROPERTY);

        ObservationScope scope = removeScope(exchange, GATEWAY_SCOPE_PROPERTY, GATEWAY_CONTEXT_PROPERTY);
        try {
            if (scope != null) {
                if (statusCode != null) {
                    scope.attribute(CommonTraceAttributes.HTTP_STATUS_CODE, statusCode);
                }
                finishScope(scope, failed ? failureOrSynthetic(failure) : null, 0L, null);
            }
        } finally {
            clearTraceMessageProperties(exchange);
            ExchangeAuthenticationContext.clear(exchange);
        }
    }

    public void startServiceExecution(Exchange exchange, ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan servicePlan) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null || exchange == null || exchange.getProperty(SERVICE_SCOPE_PROPERTY) != null) {
            return;
        }
        TraceContext parent = exchange.getProperty(GATEWAY_CONTEXT_PROPERTY, TraceContext.class);
        TraceContext requestedContext = childContext(parent, exchange);
        Service service = servicePlan != null ? servicePlan.service() : exchange.getProperty(Message.SERVICE, Service.class);
        Map<String, String> fields = exchangeMdc.fields(exchange);
        ObservationScope scope = observation.trace()
                .span("service.execute")
                .spanKind("internal")
                .action("service.execute")
                .traceId(requestedContext.traceId())
                .spanId(requestedContext.spanId())
                .parentSpanId(parent == null ? null : parent.spanId())
                .correlationId(requestedContext.correlationId())
                .correlationType(requestedContext.correlationType())
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreTraceAttributes.SERVICE_CODE, service != null ? service.getCode() : fields.get("serviceCode"))
                .attribute(CoreTraceAttributes.SERVICE_NAME, service != null ? service.getName() : null)
                .attribute(CoreTraceAttributes.SERVICE_VERSION, fields.get("serviceVersion"))
                .attribute(CoreTraceAttributes.OPERATION_CODE, fields.get("operationName"))
                .attribute(CoreTraceAttributes.OPERATION_NAME, fields.get("operationName"))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .startDetached();
        TraceContext context = startedContext(scope, requestedContext);
        exchange.setProperty(SERVICE_SCOPE_PROPERTY, scope);
        putContext(exchange, SERVICE_CONTEXT_PROPERTY, context);
    }

    public void finishServiceExecutionSuccess(Exchange exchange) {
        Throwable failure = exchangeFailure(exchange);
        finishLayerScope(
                exchange,
                SERVICE_SCOPE_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                GATEWAY_CONTEXT_PROPERTY,
                hasBusinessFailure(exchange, failure) ? failureOrSynthetic(failure) : null,
                CoreTraceAttributes.SERVICE_DURATION_MS.name(),
                serviceDurationMs(exchange)
        );
    }

    public void finishServiceExecutionFailure(Exchange exchange, Exception exception) {
        finishLayerScope(
                exchange,
                SERVICE_SCOPE_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                GATEWAY_CONTEXT_PROPERTY,
                exception,
                CoreTraceAttributes.SERVICE_DURATION_MS.name(),
                serviceDurationMs(exchange)
        );
    }

    public void finishServiceExecutionOnCompletion(Exchange exchange) {
        finishServiceExecutionSuccess(exchange);
    }

    public void startOperationCall(Exchange exchange, Operation operation) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null || exchange == null || exchange.getProperty(OPERATION_SCOPE_PROPERTY) != null) {
            return;
        }
        TraceContext parent = exchange.getProperty(SERVICE_CONTEXT_PROPERTY, TraceContext.class);
        TraceContext requestedContext = childContext(parent, exchange);
        Map<String, String> fields = exchangeMdc.fields(exchange);
        ObservationScope scope = observation.trace()
                .span("operation.call")
                .spanKind(operationSpanKind(operation))
                .action("operation.call")
                .traceId(requestedContext.traceId())
                .spanId(requestedContext.spanId())
                .parentSpanId(parent == null ? null : parent.spanId())
                .correlationId(requestedContext.correlationId())
                .correlationType(requestedContext.correlationType())
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreTraceAttributes.SERVICE_CODE, fields.get("serviceCode"))
                .attribute(CoreTraceAttributes.OPERATION_CODE, operationCode(operation))
                .attribute(CoreTraceAttributes.OPERATION_NAME, operationName(operation, fields))
                .attribute(CoreTraceAttributes.OPERATION_TYPE, operationType(operation))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .startDetached();
        TraceContext context = startedContext(scope, requestedContext);
        exchange.setProperty(OPERATION_SCOPE_PROPERTY, scope);
        putContext(exchange, OPERATION_CONTEXT_PROPERTY, context);
    }

    public void finishOperationCallSuccess(Exchange exchange, Operation operation) {
        Throwable failure = exchangeFailure(exchange);
        finishLayerScope(
                exchange,
                OPERATION_SCOPE_PROPERTY,
                OPERATION_CONTEXT_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                hasBusinessFailure(exchange, failure) ? failureOrSynthetic(failure) : null,
                CoreTraceAttributes.OPERATION_DURATION_MS.name(),
                operationDurationMs(exchange)
        );
    }

    public void finishOperationCallFailure(Exchange exchange, Operation operation, Exception exception) {
        finishLayerScope(
                exchange,
                OPERATION_SCOPE_PROPERTY,
                OPERATION_CONTEXT_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                exception,
                CoreTraceAttributes.OPERATION_DURATION_MS.name(),
                operationDurationMs(exchange)
        );
    }

    public void finishOperationCallOnCompletion(Exchange exchange, Operation operation) {
        finishOperationCallSuccess(exchange, operation);
    }

    public ObservationScope activeScope(Exchange exchange, String layer) {
        if (exchange == null || layer == null) {
            return null;
        }
        String propertyName = switch (layer.trim().toLowerCase(Locale.ROOT)) {
            case "gateway" -> GATEWAY_SCOPE_PROPERTY;
            case "service" -> SERVICE_SCOPE_PROPERTY;
            case "operation" -> OPERATION_SCOPE_PROPERTY;
            default -> null;
        };
        return propertyName == null ? null : exchange.getProperty(propertyName, ObservationScope.class);
    }

    public void enrichGatewayAuthentication(Exchange exchange, Authentication authentication) {
        if (exchange == null || authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        authenticationTraceEnrichers.orderedStream().forEach(enricher -> {
            try {
                enricher.enrich(exchange, authentication);
            } catch (RuntimeException ignored) {
                // Trace enrichment must never alter authentication or business processing.
            }
        });
    }

    private void finishLayerScope(
            Exchange exchange,
            String scopeProperty,
            String contextProperty,
            String parentContextProperty,
            Throwable exception,
            String durationField,
            long durationMs
    ) {
        if (exchange == null) {
            return;
        }
        if (exception != null) {
            exchange.setProperty(BUSINESS_FAILURE_PROPERTY, Boolean.TRUE);
        }
        ObservationScope scope = removeScope(exchange, scopeProperty, contextProperty);
        if (scope == null) {
            return;
        }
        try {
            finishScope(scope, exception, durationMs, durationField);
        } finally {
            restoreMessageContext(exchange, parentContextProperty);
        }
    }

    private ObservationScope removeScope(Exchange exchange, String scopeProperty, String contextProperty) {
        ObservationScope scope = exchange.getProperty(scopeProperty, ObservationScope.class);
        exchange.removeProperty(scopeProperty);
        exchange.removeProperty(contextProperty);
        return scope;
    }

    private void finishScope(ObservationScope scope, Throwable exception, long durationMs, String durationField) {
        try {
            if (durationField != null) {
                scope.attribute(durationField, Math.max(0L, durationMs));
            }
            if (exception == null) {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_SUCCESS).success();
            } else {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_FAILURE)
                        .attribute(CommonTraceAttributes.ERROR_TYPE, exception.getClass().getSimpleName())
                        .attribute(CommonTraceAttributes.ERROR_CODE, errorCode(exception))
                        .failure();
            }
        } finally {
            scope.close();
        }
    }

    private TraceContext childContext(TraceContext parent, Exchange exchange) {
        String traceId = firstText(parent == null ? null : parent.traceId(), property(exchange, Message.TRACE_ID), ObservationIds.traceId());
        String correlationId = firstText(parent == null ? null : parent.correlationId(), correlationId(exchange), ObservationIds.correlationId());
        String correlationType = firstText(parent == null ? null : parent.correlationType(), CorrelationType.OPERATION.value());
        return new TraceContext(traceId, ObservationIds.spanId(), correlationId, correlationType);
    }

    private TraceContext startedContext(ObservationScope scope, TraceContext requested) {
        TraceContext actual = scope == null ? null : scope.traceContext();
        return new TraceContext(
                firstText(actual == null ? null : actual.traceId(), requested == null ? null : requested.traceId()),
                firstText(actual == null ? null : actual.spanId(), requested == null ? null : requested.spanId()),
                firstText(actual == null ? null : actual.correlationId(), requested == null ? null : requested.correlationId()),
                firstText(actual == null ? null : actual.correlationType(), requested == null ? null : requested.correlationType())
        );
    }

    private void putContext(Exchange exchange, String propertyName, TraceContext context) {
        exchange.setProperty(propertyName, context);
        exchange.setProperty(Message.TRACE_ID, context.traceId());
        exchange.setProperty(Message.SPAN_ID, context.spanId());
        exchange.setProperty(Message.CORRELATION_ID, context.correlationId());
    }

    private void restoreMessageContext(Exchange exchange, String parentContextProperty) {
        TraceContext context = exchange.getProperty(parentContextProperty, TraceContext.class);
        if (context == null) {
            clearTraceMessageProperties(exchange);
            return;
        }
        exchange.setProperty(Message.TRACE_ID, context.traceId());
        exchange.setProperty(Message.SPAN_ID, context.spanId());
        exchange.setProperty(Message.CORRELATION_ID, context.correlationId());
    }

    private void clearTraceMessageProperties(Exchange exchange) {
        exchange.removeProperty(Message.TRACE_ID);
        exchange.removeProperty(Message.SPAN_ID);
        exchange.removeProperty(Message.CORRELATION_ID);
    }

    private GatewayObservationContext preparedGatewayContext(Exchange exchange) {
        HttpServletRequest request = servletRequest(exchange);
        if (request == null) {
            return null;
        }
        Object value = request.getAttribute(GatewayObservationContext.REQUEST_ATTRIBUTE);
        if (value instanceof GatewayObservationContext context) {
            return context;
        }
        return new GatewayObservationContext(
                stringAttribute(request, GatewayObservationContext.CORRELATION_ID_ATTRIBUTE),
                stringAttribute(request, GatewayObservationContext.TRACE_ID_ATTRIBUTE),
                stringAttribute(request, GatewayObservationContext.GATEWAY_SPAN_ID_ATTRIBUTE),
                stringAttribute(request, GatewayObservationContext.REMOTE_PARENT_SPAN_ID_ATTRIBUTE),
                stringAttribute(request, GatewayObservationContext.TRACE_FLAGS_ATTRIBUTE),
                stringAttribute(request, GatewayObservationContext.GATEWAY_NAME_ATTRIBUTE),
                stringAttribute(request, GatewayObservationContext.CHANNEL_CODE_ATTRIBUTE),
                RouteLogSupport.protocol(exchange),
                "server",
                requestName(request),
                null
        );
    }

    private TraceContext existingContext(Exchange exchange) {
        TraceContext gateway = exchange.getProperty(GATEWAY_CONTEXT_PROPERTY, TraceContext.class);
        if (gateway != null) {
            return gateway;
        }
        return new TraceContext(
                property(exchange, Message.TRACE_ID),
                property(exchange, Message.SPAN_ID),
                property(exchange, Message.CORRELATION_ID),
                CorrelationType.REQUEST.value()
        );
    }

    private HttpServletRequest servletRequest(Exchange exchange) {
        return exchange.getMessage().getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
    }

    private String stringAttribute(HttpServletRequest request, String name) {
        Object value = requestAttribute(request, name);
        return value == null ? null : String.valueOf(value);
    }

    private Object requestAttribute(HttpServletRequest request, String name) {
        return request == null ? null : request.getAttribute(name);
    }

    private String requestName(HttpServletRequest request) {
        return request == null ? null : "HTTP " + firstText(request.getMethod(), "unknown");
    }

    private String httpMethod(Exchange exchange, HttpServletRequest request) {
        return firstText(
                request == null ? null : request.getMethod(),
                header(exchange, Constants.CAMEL_PARAMETER_HTTP_METHOD),
                exchange.getMessage().getHeader(Exchange.HTTP_METHOD, String.class)
        );
    }

    private String requestPath(Exchange exchange, HttpServletRequest request) {
        return safePath(firstText(
                request == null ? null : request.getRequestURI(),
                header(exchange, Constants.CAMEL_PARAMETER_HTTP_URI),
                exchange.getMessage().getHeader(Exchange.HTTP_URI, String.class)
        ));
    }

    private boolean hasQuery(Exchange exchange, HttpServletRequest request) {
        String query = firstText(
                request == null ? null : request.getQueryString(),
                exchange.getMessage().getHeader(Exchange.HTTP_QUERY, String.class)
        );
        return query != null;
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int separator = forwardedFor.indexOf(',');
            String first = separator < 0 ? forwardedFor : forwardedFor.substring(0, separator);
            if (!first.isBlank()) {
                return first.trim();
            }
        }
        return firstText(request.getRemoteAddr());
    }

    private String correlationId(Exchange exchange) {
        return firstText(
                exchange.getMessage().getHeader("X-Correlation-Id", String.class),
                exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class),
                exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class),
                property(exchange, Message.CORRELATION_ID)
        );
    }

    private String property(Exchange exchange, String name) {
        Object value = exchange.getProperty(name);
        return value == null ? null : String.valueOf(value);
    }

    private String header(Exchange exchange, String name) {
        return exchange.getMessage().getHeader(name, String.class);
    }

    private Integer httpStatus(Exchange exchange) {
        return exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
    }

    private Throwable exchangeFailure(Exchange exchange) {
        Throwable failure = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        return failure == null ? exchange.getException() : failure;
    }

    private boolean hasBusinessFailure(Exchange exchange, Throwable failure) {
        if (exchange == null) {
            return failure != null;
        }
        Integer statusCode = httpStatus(exchange);
        return failure != null
                || Boolean.TRUE.equals(exchange.getProperty(BUSINESS_FAILURE_PROPERTY, Boolean.class))
                || statusCode != null && statusCode >= 400
                || businessFailureBody(exchange.getMessage().getBody());
    }

    private Throwable failureOrSynthetic(Throwable failure) {
        return failure == null ? new BusinessFailure() : failure;
    }

    private boolean businessFailureBody(Object body) {
        if (body instanceof ScmFault) {
            return true;
        }
        if (body instanceof Message message) {
            MessageStatus status = message.getStatus();
            return status != null && status != MessageStatus.SC_SUCCESS && status != MessageStatus.SC_PROCESSING;
        }
        return false;
    }

    private String safePath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return URI.create(value).getPath();
        } catch (IllegalArgumentException ignored) {
            return value.contains("?") ? value.substring(0, value.indexOf('?')) : value;
        }
    }

    private String errorCode(Throwable exception) {
        if (exception instanceof ErrorCodeAwareException aware) {
            return String.valueOf(aware.getErrorCode());
        }
        return exception == null ? null : exception.getClass().getSimpleName();
    }

    private long serviceDurationMs(Exchange exchange) {
        return RouteLogSupport.durationMs(exchange, RouteLogSupport.SERVICE_START_NANOS);
    }

    private long operationDurationMs(Exchange exchange) {
        return RouteLogSupport.durationMs(exchange, RouteLogSupport.OPERATION_START_NANOS);
    }

    private String operationSpanKind(Operation operation) {
        OperationType type = operation != null ? operation.getType() : null;
        return Objects.equals(type, OperationType.PROVIDER) || Objects.equals(type, OperationType.REST)
                ? "client"
                : "internal";
    }

    private String operationCode(Operation operation) {
        return operation != null ? operation.getName() : null;
    }

    private String operationName(Operation operation, Map<String, String> fields) {
        if (operation != null && operation.getName() != null) {
            return operation.getName();
        }
        return fields.get("operationName");
    }

    private String operationType(Operation operation) {
        return operation != null && operation.getType() != null ? operation.getType().name() : null;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static final class BusinessFailure extends RuntimeException {
        private BusinessFailure() {
            super("SCM business failure", null, false, false);
        }
    }
}
