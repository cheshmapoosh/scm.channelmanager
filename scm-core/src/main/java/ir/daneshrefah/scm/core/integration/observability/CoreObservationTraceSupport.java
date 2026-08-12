package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowExchangeProperties;
import ir.daneshrefah.scm.observation.starter.*;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.observation.starter.gateway.GatewayObservationContext;
import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    private static final String ROUTING_STEP_OWNS_OPERATION_SCOPE_PROPERTY =
            "scm.observation.scope.operation.routing-step-owned";

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
                CorrelationType.REQUEST.value(),
                preparedContext == null ? null : preparedContext.traceFlags()
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
                .traceFlags(requestedContext.traceFlags())
                .correlationId(requestedContext.correlationId())
                .correlationType(requestedContext.correlationType())
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, firstText(
                        preparedContext == null ? null : preparedContext.gatewayName(),
                        fields.get("gatewayName")))
                .attribute(CoreTraceAttributes.GATEWAY_CHANNEL_CODE, gatewayChannelCode(exchange, preparedContext, fields))
                .attribute(CoreTraceAttributes.CLIENT_ID, header(exchange, Constants.SCM_PARAMETER_CLIENT_ID))
                .attribute(CoreTraceAttributes.CLIENT_CHANNEL_CODE, clientDeclaredChannelCode(exchange))
                .attribute(CoreTraceAttributes.CLIENT_CHANNEL_VALIDATED, clientChannelValidated(exchange))
                .attribute(CoreTraceAttributes.CLIENT_USERNAME, header(exchange, Constants.SCM_PARAMETER_USERNAME))
                .attribute(CoreTraceAttributes.CLIENT_ADDRESS, clientAddress(exchange, servletRequest))
                .attribute(CoreTraceAttributes.CLIENT_CORRELATION_ID, header(exchange, Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID))
                .attribute(CoreTraceAttributes.SERVICE_ID, serviceId(service))
                .attribute(CoreTraceAttributes.SERVICE_CODE, firstText(
                        service == null ? null : service.getCode(),
                        fields.get("serviceCode")
                ))
                .attribute(CoreTraceAttributes.SERVICE_NAME, firstText(
                        service == null ? null : service.getName()
                ))
                .attribute(CoreTraceAttributes.SERVICE_VERSION, fields.get("serviceVersion"))
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
        attributes.put(CommonTraceAttributes.HTTP_RESPONSE_BODY_SIZE.name(), bodySize(exchange.getMessage().getBody()));
        String contentType = responseContentType(exchange);
        if (contentType != null) {
            attributes.put(CommonTraceAttributes.HTTP_RESPONSE_HEADER_CONTENT_TYPE.name(), contentType);
        }
        gatewayScope.event("gateway.response.completed", attributes);
    }

    public void finishGatewayReceive(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        Throwable failure = exchangeFailure(exchange);
        FailureDetails failureDetails = failureDetails(exchange, failure);
        boolean failed = failureDetails != null;
        exchange.removeProperty(BUSINESS_FAILURE_PROPERTY);
        exchange.removeProperty(GATEWAY_RESPONSE_EVENT_PROPERTY);

        ObservationScope scope = removeScope(exchange, GATEWAY_SCOPE_PROPERTY, GATEWAY_CONTEXT_PROPERTY);
        try {
            if (scope != null) {
                enrichGatewayMessageId(exchange, scope);
                finishScope(scope, failed ? failure : null, failureDetails, 0L, null);
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
                .traceFlags(requestedContext.traceFlags())
                .correlationId(requestedContext.correlationId())
                .correlationType(requestedContext.correlationType())
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreTraceAttributes.SERVICE_ID, serviceId(service))
                .attribute(CoreTraceAttributes.SERVICE_CODE, firstText(
                        service == null ? null : service.getCode(),
                        fields.get("serviceCode")
                ))
                .attribute(CoreTraceAttributes.SERVICE_NAME, firstText(
                        service == null ? null : service.getName()
                ))
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
        FailureDetails failureDetails = failureDetails(exchange, failure);
        finishLayerScope(
                exchange,
                SERVICE_SCOPE_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                GATEWAY_CONTEXT_PROPERTY,
                failure,
                failureDetails,
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
                failureDetails(exchange, exception),
                CoreTraceAttributes.SERVICE_DURATION_MS.name(),
                serviceDurationMs(exchange)
        );
    }

    public void finishServiceExecutionOnCompletion(Exchange exchange) {
        finishServiceExecutionSuccess(exchange);
    }

    public void startOperationCall(Exchange exchange, Operation operation) {
        if (routingStepOwnsOperationScope(exchange)) {
            enrichRoutingStepOperation(exchange, operation);
            return;
        }
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
                .traceFlags(requestedContext.traceFlags())
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
        if (routingStepOwnsOperationScope(exchange)) {
            return;
        }
        Throwable failure = exchangeFailure(exchange);
        FailureDetails failureDetails = failureDetails(exchange, failure);
        finishLayerScope(
                exchange,
                OPERATION_SCOPE_PROPERTY,
                OPERATION_CONTEXT_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                failure,
                failureDetails,
                CoreTraceAttributes.OPERATION_DURATION_MS.name(),
                operationDurationMs(exchange)
        );
    }

    public void finishOperationCallFailure(Exchange exchange, Operation operation, Exception exception) {
        if (routingStepOwnsOperationScope(exchange)) {
            return;
        }
        finishLayerScope(
                exchange,
                OPERATION_SCOPE_PROPERTY,
                OPERATION_CONTEXT_PROPERTY,
                SERVICE_CONTEXT_PROPERTY,
                exception,
                failureDetails(exchange, exception),
                CoreTraceAttributes.OPERATION_DURATION_MS.name(),
                operationDurationMs(exchange)
        );
    }

    public void finishOperationCallOnCompletion(Exchange exchange, Operation operation) {
        finishOperationCallSuccess(exchange, operation);
    }

    public void startRoutingStepCall(
            Exchange exchange,
            String serviceCode,
            String operationName,
            RoutingStrategy routingStrategy,
            String stepId,
            int stepIndex,
            String inboundAction,
            TaskWorkflowStepType taskWorkflowStepType
    ) {
        startRoutingStepCall(
                exchange,
                serviceCode,
                operationName,
                routingStrategy,
                stepId,
                stepIndex,
                inboundAction,
                taskWorkflowStepType,
                "internal"
        );
    }

    public void startRoutingStepCall(
            Exchange exchange,
            String serviceCode,
            String operationName,
            RoutingStrategy routingStrategy,
            String stepId,
            int stepIndex,
            String inboundAction,
            TaskWorkflowStepType taskWorkflowStepType,
            String spanKind
    ) {
        if (exchange == null || exchange.getProperty(OPERATION_SCOPE_PROPERTY) != null) {
            return;
        }
        ObservationScope scope = null;
        try {
            ScmObservation observation = observationProvider.getIfAvailable();
            if (observation == null) {
                return;
            }
            TraceContext parent = exchange.getProperty(SERVICE_CONTEXT_PROPERTY, TraceContext.class);
            TraceContext requestedContext = childContext(parent, exchange);
            Map<String, String> fields = exchangeMdc.fields(exchange);
            scope = observation.trace()
                    .span("operation.call")
                    .spanKind(firstText(spanKind, "internal"))
                    .action("operation.call")
                    .traceId(requestedContext.traceId())
                    .spanId(requestedContext.spanId())
                    .parentSpanId(parent == null ? null : parent.spanId())
                    .traceFlags(requestedContext.traceFlags())
                    .correlationId(requestedContext.correlationId())
                    .correlationType(requestedContext.correlationType())
                    .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                    .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                    .attribute(CoreTraceAttributes.SERVICE_CODE, firstText(serviceCode, fields.get("serviceCode")))
                    .attribute(CoreTraceAttributes.OPERATION_CODE, operationName)
                    .attribute(CoreTraceAttributes.OPERATION_NAME, operationName)
                    .attribute(CoreTraceAttributes.ROUTING_STRATEGY,
                            routingStrategy == null ? null : routingStrategy.name())
                    .attribute(CoreTraceAttributes.ROUTING_STEP_ID, stepId)
                    .attribute(CoreTraceAttributes.ROUTING_STEP_INDEX, (long) stepIndex)
                    .attribute(CoreTraceAttributes.ROUTING_EXECUTION_ID,
                            exchange.getProperty(Message.EXECUTION_ID, String.class))
                    .attribute(CoreTraceAttributes.TASK_INBOUND_ACTION, inboundAction)
                    .attribute(
                            CoreTraceAttributes.TASK_ACTION_PLAN_NAME,
                            exchange.getProperty(
                                    TaskWorkflowExchangeProperties.ACTION_PLAN_NAME,
                                    String.class
                            )
                    )
                    .attribute(CoreTraceAttributes.TASK_WORKFLOW_STEP_TYPE,
                            taskWorkflowStepType == null ? null : taskWorkflowStepType.name())
                    .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                    .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                    .startDetached();
            TraceContext context = startedContext(scope, requestedContext);
            exchange.setProperty(OPERATION_SCOPE_PROPERTY, scope);
            exchange.setProperty(ROUTING_STEP_OWNS_OPERATION_SCOPE_PROPERTY, Boolean.TRUE);
            putContext(exchange, OPERATION_CONTEXT_PROPERTY, context);
        } catch (RuntimeException ignored) {
            // Observation must never change routing behavior.
            exchange.removeProperty(ROUTING_STEP_OWNS_OPERATION_SCOPE_PROPERTY);
            exchange.removeProperty(OPERATION_SCOPE_PROPERTY);
            exchange.removeProperty(OPERATION_CONTEXT_PROPERTY);
            if (scope != null) {
                scope.close();
            }
            restoreMessageContext(exchange, SERVICE_CONTEXT_PROPERTY);
        }
    }

    public void finishRoutingStepCall(
            Exchange exchange,
            String decision,
            String normalizedOutcome,
            Throwable failure,
            long durationMs
    ) {
        if (exchange == null || !routingStepOwnsOperationScope(exchange)) {
            return;
        }
        ObservationScope scope = exchange.getProperty(OPERATION_SCOPE_PROPERTY, ObservationScope.class);
        exchange.removeProperty(ROUTING_STEP_OWNS_OPERATION_SCOPE_PROPERTY);
        exchange.removeProperty(OPERATION_SCOPE_PROPERTY);
        exchange.removeProperty(OPERATION_CONTEXT_PROPERTY);
        try {
            if (scope == null) {
                return;
            }
            scope.attribute(CoreTraceAttributes.OPERATION_DURATION_MS, Math.max(0L, durationMs))
                    .attribute(CoreTraceAttributes.ROUTING_DECISION, decision)
                    .attribute(CoreTraceAttributes.ROUTING_RETRYABLE,
                            "RETRY_LATER".equals(decision))
                    .attribute(CoreTraceAttributes.OPERATION_NORMALIZED_OUTCOME, normalizedOutcome);
            FailureDetails details = "SUCCESS".equals(decision)
                    ? null
                    : failureDetails(exchange, failure);
            if (details != null) {
                scope.attribute(CommonTraceAttributes.ERROR_TYPE, details.errorType())
                        .attribute(CommonTraceAttributes.ERROR_CODE, details.errorCode());
            }
            if ("SUCCESS".equals(decision)) {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_SUCCESS).success();
            } else if ("RETRY_LATER".equals(decision)) {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, "unknown").outcome("unknown");
            } else if (failure == null) {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_FAILURE).failure();
            } else {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_FAILURE).failure(failure);
            }
        } catch (RuntimeException ignored) {
            // Observation must never change routing behavior.
        } finally {
            try {
                if (scope != null) {
                    scope.close();
                }
            } finally {
                restoreMessageContext(exchange, SERVICE_CONTEXT_PROPERTY);
            }
        }
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
            FailureDetails failureDetails,
            String durationField,
            long durationMs
    ) {
        if (exchange == null) {
            return;
        }
        if (failureDetails != null) {
            exchange.setProperty(BUSINESS_FAILURE_PROPERTY, Boolean.TRUE);
        }
        ObservationScope scope = removeScope(exchange, scopeProperty, contextProperty);
        if (scope == null) {
            return;
        }
        try {
            finishScope(scope, exception, failureDetails, durationMs, durationField);
        } finally {
            restoreMessageContext(exchange, parentContextProperty);
        }
    }

    private boolean routingStepOwnsOperationScope(Exchange exchange) {
        return exchange != null && Boolean.TRUE.equals(exchange.getProperty(
                ROUTING_STEP_OWNS_OPERATION_SCOPE_PROPERTY,
                Boolean.class
        ));
    }

    private void enrichRoutingStepOperation(Exchange exchange, Operation operation) {
        ObservationScope scope = exchange.getProperty(OPERATION_SCOPE_PROPERTY, ObservationScope.class);
        if (scope == null) {
            return;
        }
        try {
            Map<String, String> fields = exchangeMdc.fields(exchange);
            scope.attribute(CoreTraceAttributes.OPERATION_CODE, operationCode(operation))
                    .attribute(CoreTraceAttributes.OPERATION_NAME, operationName(operation, fields))
                    .attribute(CoreTraceAttributes.OPERATION_TYPE, operationType(operation));
        } catch (RuntimeException ignored) {
            // Observation enrichment must never change operation execution.
        }
    }

    private ObservationScope removeScope(Exchange exchange, String scopeProperty, String contextProperty) {
        ObservationScope scope = exchange.getProperty(scopeProperty, ObservationScope.class);
        exchange.removeProperty(scopeProperty);
        exchange.removeProperty(contextProperty);
        return scope;
    }

    private void finishScope(
            ObservationScope scope,
            Throwable exception,
            FailureDetails failureDetails,
            long durationMs,
            String durationField
    ) {
        try {
            if (durationField != null) {
                scope.attribute(durationField, Math.max(0L, durationMs));
            }
            if (failureDetails == null) {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_SUCCESS).success();
            } else {
                scope.attribute(CommonTraceAttributes.EVENT_OUTCOME, OUTCOME_FAILURE)
                        .attribute(CommonTraceAttributes.ERROR_TYPE, failureDetails.errorType())
                        .attribute(CommonTraceAttributes.ERROR_CODE, failureDetails.errorCode());
                if (exception == null) {
                    scope.failure();
                } else {
                    scope.failure(exception);
                }
            }
        } finally {
            scope.close();
        }
    }

    private TraceContext childContext(TraceContext parent, Exchange exchange) {
        String traceId = firstText(parent == null ? null : parent.traceId(), property(exchange, Message.TRACE_ID), ObservationIds.traceId());
        String correlationId = firstText(parent == null ? null : parent.correlationId(), correlationId(exchange), ObservationIds.correlationId());
        String correlationType = firstText(parent == null ? null : parent.correlationType(), CorrelationType.OPERATION.value());
        String traceFlags = firstText(parent == null ? null : parent.traceFlags());
        return new TraceContext(traceId, ObservationIds.spanId(), correlationId, correlationType, traceFlags);
    }

    private TraceContext startedContext(ObservationScope scope, TraceContext requested) {
        TraceContext actual = scope == null ? null : scope.traceContext();
        return new TraceContext(
                firstText(actual == null ? null : actual.traceId(), requested == null ? null : requested.traceId()),
                firstText(actual == null ? null : actual.spanId(), requested == null ? null : requested.spanId()),
                firstText(actual == null ? null : actual.correlationId(), requested == null ? null : requested.correlationId()),
                firstText(actual == null ? null : actual.correlationType(), requested == null ? null : requested.correlationType()),
                firstText(actual == null ? null : actual.traceFlags(), requested == null ? null : requested.traceFlags())
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
                CorrelationType.REQUEST.value(),
                null
        );
    }

    private HttpServletRequest servletRequest(Exchange exchange) {
        if (exchange == null || exchange.getMessage() == null) {
            return null;
        }

        HttpServletRequest headerRequest = exchange.getMessage().getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
        if (headerRequest != null) {
            return headerRequest;
        }

        if (exchange.getMessage() instanceof HttpMessage httpMessage) {
            return httpMessage.getRequest();
        }

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }

        return null;
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
        return firstText(firstForwardedAddress(request.getHeader("X-Forwarded-For")), request.getHeader("X-Real-IP"), request.getRemoteAddr());
    }

    private String gatewayChannelCode(
            Exchange exchange,
            GatewayObservationContext preparedContext,
            Map<String, String> fields
    ) {
        GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
        return firstText(
                gatewayChannel != null && gatewayChannel.getChannel() != null
                        ? gatewayChannel.getChannel().getCode()
                        : null,
                preparedContext == null ? null : preparedContext.channelCode(),
                fields == null ? null : fields.get("channelCode")
        );
    }

    private String clientDeclaredChannelCode(Exchange exchange) {
        return firstText(
                header(exchange, IncomingChannelCodeResolver.SCM_CHANNEL_HEADER),
                header(exchange, IncomingChannelCodeResolver.CHANNEL_CODE_HEADER),
                header(exchange, Message.CHANNEL_CODE)
        );
    }

    private Boolean clientChannelValidated(Exchange exchange) {
        String declared = clientDeclaredChannelCode(exchange);
        if (declared == null) {
            return null;
        }
        GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
        String configured = gatewayChannel != null && gatewayChannel.getChannel() != null
                ? gatewayChannel.getChannel().getCode()
                : null;
        return configured != null && configured.equalsIgnoreCase(declared);
    }

    private String clientAddress(Exchange exchange, HttpServletRequest request) {
        return firstText(
                stringAttribute(request, CoreTraceAttributes.CLIENT_ADDRESS.name()),
                stringAttribute(request, CommonTraceAttributes.CLIENT_IP.name()),
                clientIp(request),
                firstForwardedAddress(exchange.getMessage().getHeader("X-Forwarded-For", String.class)),
                exchange.getMessage().getHeader("X-Real-IP", String.class),
                exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_REMOTE_ADDRESS, String.class)
        );
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

    private long bodySize(Object body) {
        if (body == null) {
            return 0L;
        }
        if (body instanceof byte[] bytes) {
            return bytes.length;
        }
        if (body instanceof ByteBuffer buffer) {
            return buffer.asReadOnlyBuffer().remaining();
        }
        return String.valueOf(body).getBytes(StandardCharsets.UTF_8).length;
    }

    private String responseContentType(Exchange exchange) {
        return firstText(
                exchange.getMessage().getHeader(Exchange.CONTENT_TYPE, String.class),
                exchange.getMessage().getHeader("Content-Type", String.class)
        );
    }

    private Throwable exchangeFailure(Exchange exchange) {
        Throwable failure = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        return failure == null ? exchange.getException() : failure;
    }

    private FailureDetails failureDetails(Exchange exchange, Throwable failure) {
        if (failure != null) {
            return new FailureDetails(errorCode(failure), failure.getClass().getSimpleName());
        }
        if (exchange == null) {
            return null;
        }
        ProviderBusinessOutcome providerOutcome = exchange.getProperty(
                ProviderBusinessOutcome.EXCHANGE_PROPERTY,
                ProviderBusinessOutcome.class
        );
        if (providerOutcome != null && !providerOutcome.success()) {
            return new FailureDetails(providerOutcome.safeErrorCode(), providerOutcome.errorType());
        }
        Integer statusCode = httpStatus(exchange);
        if (Boolean.TRUE.equals(exchange.getProperty(BUSINESS_FAILURE_PROPERTY, Boolean.class))) {
            return new FailureDetails(null, ProviderBusinessOutcome.BUSINESS_ERROR_TYPE);
        }
        if (statusCode != null && statusCode >= 400) {
            return new FailureDetails(String.valueOf(statusCode), ProviderBusinessOutcome.BUSINESS_ERROR_TYPE);
        }
        return businessFailureBody(exchange.getMessage().getBody());
    }

    private FailureDetails businessFailureBody(Object body) {
        if (body instanceof ScmFault) {
            return new FailureDetails(null, ProviderBusinessOutcome.BUSINESS_ERROR_TYPE);
        }
        if (body instanceof Message message) {
            MessageStatus status = message.getStatus();
            if (status != null && status != MessageStatus.SC_SUCCESS && status != MessageStatus.SC_PROCESSING) {
                return new FailureDetails(status.name(), ProviderBusinessOutcome.BUSINESS_ERROR_TYPE);
            }
        }
        return null;
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

    private record FailureDetails(String errorCode, String errorType) {
        private FailureDetails {
            errorCode = errorCode == null || errorCode.isBlank() ? null : errorCode.trim();
            errorType = errorType == null || errorType.isBlank()
                    ? ProviderBusinessOutcome.BUSINESS_ERROR_TYPE
                    : errorType.trim();
        }
    }

    private void enrichGatewayMessageId(Exchange exchange, ObservationScope gatewayScope) {
        if (exchange == null || gatewayScope == null) {
            return;
        }

        Message internalMessage = exchange.getProperty(Message.INTERNAL_MESSAGE, Message.class);

        if (internalMessage == null || internalMessage.getHeader() == null) {
            return;
        }

        String messageId = internalMessage.getHeader().getMessageId();
        if (messageId == null || messageId.isBlank()) {
            return;
        }

        gatewayScope.attribute(CommonTraceAttributes.SCM_MESSAGE_ID, messageId);
    }

    private Integer serviceId(Service service) {
        return service == null || service.getId() == null
                ? null
                : service.getId().intValue();
    }

    private String firstForwardedAddress(String forwardedFor) {
        String value = firstText(forwardedFor);

        if (value == null) {
            return null;
        }

        int separator = value.indexOf(',');

        return firstText(
                separator < 0
                        ? value
                        : value.substring(0, separator)
        );
    }

}
