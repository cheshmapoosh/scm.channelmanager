package ir.daneshrefah.scm.core.integration.observability;

import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreTraceAttributes;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.utils.constant.Constants;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Component
public class CoreObservationTraceSupport {
    private static final String SERVICE_SCOPE_PROPERTY = "scmServiceExecuteObservationScope";
    private static final String OPERATION_SCOPE_PROPERTY = "scmOperationCallObservationScope";

    private final ObjectProvider<ScmObservation> observationProvider;
    private final ScmExchangeMdc exchangeMdc;

    CoreObservationTraceSupport(
            ObjectProvider<ScmObservation> observationProvider,
            ScmExchangeMdc exchangeMdc
    ) {
        this.observationProvider = observationProvider;
        this.exchangeMdc = exchangeMdc;
    }

    public void traceGatewayRequest(Exchange exchange, Service service) {
        writeTrace("gateway.request", "gateway.request.received", "success", exchange, service, null, null);
    }

    public void traceGatewayResponse(Exchange exchange, Service service) {
        writeTrace("gateway.response", "gateway.response.sent", "success", exchange, service, null, null);
    }

    public void startServiceExecution(Exchange exchange, ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan servicePlan) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null || exchange == null || exchange.getProperty(SERVICE_SCOPE_PROPERTY) != null) {
            return;
        }
        Map<String, String> fields = exchangeMdc.fields(exchange);
        Service service = servicePlan != null ? servicePlan.service() : exchange.getProperty(Message.SERVICE, Service.class);
        ObservationScope scope = observation.trace()
                .span("service.execute")
                .spanKind("internal")
                .action("service.execute")
                .correlationId(fields.get("correlationId"))
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreTraceAttributes.SERVICE_CODE, service != null ? service.getCode() : fields.get("serviceCode"))
                .attribute(CoreTraceAttributes.SERVICE_NAME, service != null ? service.getName() : null)
                .attribute(CoreTraceAttributes.SERVICE_VERSION, fields.get("serviceVersion"))
                .attribute(CoreTraceAttributes.OPERATION_CODE, fields.get("operationName"))
                .attribute(CoreTraceAttributes.OPERATION_NAME, fields.get("operationName"))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .start();
        exchange.setProperty(SERVICE_SCOPE_PROPERTY, scope);
    }

    public void finishServiceExecutionSuccess(Exchange exchange) {
        finishScope(exchange, SERVICE_SCOPE_PROPERTY, null, CoreTraceAttributes.SERVICE_DURATION_MS.name(), serviceDurationMs(exchange));
    }

    public void finishServiceExecutionFailure(Exchange exchange, Exception exception) {
        finishScope(exchange, SERVICE_SCOPE_PROPERTY, exception, CoreTraceAttributes.SERVICE_DURATION_MS.name(), serviceDurationMs(exchange));
    }

    public void startOperationCall(Exchange exchange, Operation operation) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null || exchange == null || exchange.getProperty(OPERATION_SCOPE_PROPERTY) != null) {
            return;
        }
        Map<String, String> fields = exchangeMdc.fields(exchange);
        ObservationScope scope = observation.trace()
                .span("operation.call")
                .spanKind(operationSpanKind(operation))
                .action("operation.call")
                .correlationId(fields.get("correlationId"))
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CoreTraceAttributes.SERVICE_CODE, fields.get("serviceCode"))
                .attribute(CoreTraceAttributes.OPERATION_CODE, operationCode(operation))
                .attribute(CoreTraceAttributes.OPERATION_NAME, operationName(operation, fields))
                .attribute(CoreTraceAttributes.OPERATION_TYPE, operationType(operation))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .start();
        exchange.setProperty(OPERATION_SCOPE_PROPERTY, scope);
    }

    public void finishOperationCallSuccess(Exchange exchange, Operation operation) {
        finishScope(exchange, OPERATION_SCOPE_PROPERTY, null, CoreTraceAttributes.OPERATION_DURATION_MS.name(), operationDurationMs(exchange));
    }

    public void finishOperationCallFailure(Exchange exchange, Operation operation, Exception exception) {
        finishScope(exchange, OPERATION_SCOPE_PROPERTY, exception, CoreTraceAttributes.OPERATION_DURATION_MS.name(), operationDurationMs(exchange));
    }

    public void traceException(Exchange exchange, Exception exception) {
        writeTrace("route.exception", "route.exception", "failure", exchange, null, null, exception);
    }

    private void finishScope(Exchange exchange, String propertyName, Exception exception, String durationField, long durationMs) {
        if (exchange == null) {
            return;
        }
        ObservationScope scope = exchange.getProperty(propertyName, ObservationScope.class);
        if (scope == null) {
            return;
        }
        exchange.removeProperty(propertyName);
        try {
            scope.attribute("event.outcome", exception == null ? "success" : "failure")
                    .attribute(durationField, durationMs);
            if (exception == null) {
                scope.success();
            } else {
                scope.failure(exception)
                        .attribute(CommonTraceAttributes.ERROR_TYPE, exception.getClass().getName())
                        .attribute(CommonTraceAttributes.ERROR_MESSAGE, RouteLogSupport.failureMessage(exception));
            }
        } finally {
            scope.close();
        }
    }

    private void writeTrace(
            String spanName,
            String action,
            String outcome,
            Exchange exchange,
            Service service,
            Operation operation,
            Exception exception
    ) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null || exchange == null) {
            return;
        }

        Map<String, String> fields = exchangeMdc.fields(exchange);
        var trace = observation.trace()
                .span(spanName)
                .spanKind("internal")
                .action(action)
                .outcome(outcome)
                .correlationId(fields.get("correlationId"))
                .attribute(CommonTraceAttributes.SCM_GATEWAY_NAME, fields.get("gatewayName"))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, fields.get("channelCode"))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, fields.get("routeId"))
                .attribute(CoreTraceAttributes.EXCHANGE_ID, fields.get("exchangeId"))
                .attribute(CommonTraceAttributes.SCM_PROTOCOL, RouteLogSupport.protocol(exchange))
                .attribute(CoreTraceAttributes.TARGET_KIND, RouteLogSupport.targetKind(exchange))
                .attribute(CommonTraceAttributes.HTTP_METHOD, header(exchange, Constants.CAMEL_PARAMETER_HTTP_METHOD))
                .attribute(CommonTraceAttributes.URL_PATH, safePath(header(exchange, Constants.CAMEL_PARAMETER_HTTP_URI)))
                .attribute(CommonTraceAttributes.HTTP_STATUS_CODE, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));

        Service resolvedService = service != null ? service : exchange.getProperty(Message.SERVICE, Service.class);
        if (resolvedService != null) {
            trace.attribute(CoreTraceAttributes.SERVICE_CODE, resolvedService.getCode())
                    .attribute(CoreTraceAttributes.SERVICE_NAME, resolvedService.getName())
                    .attribute(CoreTraceAttributes.SERVICE_VERSION, fields.get("serviceVersion"));
        } else {
            trace.attribute(CoreTraceAttributes.SERVICE_CODE, fields.get("serviceCode"))
                    .attribute(CoreTraceAttributes.SERVICE_VERSION, fields.get("serviceVersion"));
        }

        Operation resolvedOperation = operation != null ? operation : exchange.getProperty(Message.OPERATION, Operation.class);
        if (resolvedOperation != null) {
            trace.attribute(CoreTraceAttributes.OPERATION_CODE, resolvedOperation.getName())
                    .attribute(CoreTraceAttributes.OPERATION_NAME, resolvedOperation.getName())
                    .attribute(CoreTraceAttributes.OPERATION_TYPE, resolvedOperation.getType() != null ? resolvedOperation.getType().name() : null);
        } else {
            trace.attribute(CoreTraceAttributes.OPERATION_CODE, fields.get("operationName"));
        }

        if (exception != null) {
            trace.attribute(CommonTraceAttributes.ERROR_TYPE, exception.getClass().getName())
                    .attribute(CommonTraceAttributes.ERROR_MESSAGE, RouteLogSupport.failureMessage(exception));
        }

        ObservationScope scope = trace.start();
        try {
            if ("failure".equals(outcome)) {
                scope.failure();
            } else {
                scope.success();
            }
        } finally {
            scope.close();
        }
    }

    private String header(Exchange exchange, String name) {
        return exchange.getMessage().getHeader(name, String.class);
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
}
