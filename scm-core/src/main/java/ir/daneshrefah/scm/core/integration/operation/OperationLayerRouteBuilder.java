package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.observability.PluginObservationSupport;
import ir.daneshrefah.scm.core.integration.observability.RouteLogEvents;
import ir.daneshrefah.scm.core.integration.observability.RouteLogSupport;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.operation.handler.OperationTypeHandler;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetProperties;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.core.integration.observability.attributes.CoreMetricTags;
import ir.daneshrefah.scm.observation.starter.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.starter.metrics.MetricCounterBuilder;
import ir.daneshrefah.scm.observation.starter.metrics.MetricTimerBuilder;
import ir.daneshrefah.scm.observation.starter.metrics.CommonMetricNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLayerRouteBuilder extends RouteBuilder {
    private final RuntimeRouteActivation runtimeRouteActivation;
    private final GatewayService gatewayService;
    private final RuntimeRoutePlanProvider runtimeRoutePlanProvider;
    private final OperationService operationService;
    private final PluginResolverService pluginResolverService;
    private final Map<String, PluginHandler> pluginHandlers;
    private final List<OperationTypeHandler> operationTypeHandlers;
    private final GlobalErrorHandler globalErrorHandler;
    private final CoreObservationTraceSupport observationTraceSupport;
    private final PluginObservationSupport pluginObservationSupport;
    private final ScmExchangeMdc exchangeMdc;
    private final ObjectProvider<ScmObservation> observationProvider;
    private final ObjectProvider<ObservationContext> observationContextProvider;

    @Override
    public void configure() {
        List<RuntimeTargetProperties> runtimeTargets = runtimeRouteActivation.runtimeTargets();
        log.info("event={} layer=operation targetCount={} outcome=started",
                RouteLogEvents.OPERATION_ROUTE_CONSTRUCTION_STARTED,
                runtimeTargets.size());

        Set<String> requiredOperationNames = resolveRequiredOperationNames(runtimeTargets);
        log.info("event={} layer=operation requiredOperationCount={} runtimeTargetCount={} outcome=success",
                RouteLogEvents.OPERATION_ROUTE_PLAN_RESOLVED,
                requiredOperationNames.size(),
                runtimeTargets.size());

        if (requiredOperationNames.isEmpty()) {
            log.warn("event={} layer=operation requiredOperationCount=0 runtimeTargetCount={} outcome=skipped reason=no-required-operations",
                    RouteLogEvents.OPERATION_ROUTE_SKIPPED,
                    runtimeTargets.size());
            log.info("event={} layer=operation requiredOperationCount=0 builtRouteCount=0 skippedOperationCount=0 outcome=success",
                    RouteLogEvents.OPERATION_ROUTE_CONSTRUCTION_COMPLETED);
            return;
        }

        List<Operation> operations = operationService.findActiveOperationsByNames(requiredOperationNames);
        Map<String, Operation> activeOperationsByName = activeOperationsByRequiredName(requiredOperationNames, operations);
        logMissingOperations(requiredOperationNames, activeOperationsByName.keySet());

        int builtRouteCount = 0;
        for (Operation operation : activeOperationsByName.values()) {
            buildOperationRoute(operation);
            builtRouteCount++;
        }
        log.info("event={} layer=operation requiredOperationCount={} loadedOperationCount={} builtRouteCount={} outcome=success",
                RouteLogEvents.OPERATION_ROUTE_CONSTRUCTION_COMPLETED,
                requiredOperationNames.size(),
                activeOperationsByName.size(),
                builtRouteCount);
    }

    private Set<String> resolveRequiredOperationNames(List<RuntimeTargetProperties> runtimeTargets) {
        Set<String> requiredOperationNames = new LinkedHashSet<>();
        for (RuntimeTargetProperties runtimeTarget : runtimeTargets) {
            if (runtimeTarget == null || !runtimeTarget.enabled()) {
                continue;
            }
            for (String gatewayName : runtimeTarget.gatewayNames()) {
                resolveRequiredOperationNames(runtimeTarget, gatewayName, requiredOperationNames);
            }
        }
        return requiredOperationNames;
    }

    private void resolveRequiredOperationNames(RuntimeTargetProperties runtimeTarget,
                                               String gatewayName,
                                               Set<String> requiredOperationNames) {
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(gatewayName);
        if (gatewayChannel == null) {
            log.warn("event={} layer=operation gatewayName={} reason=gateway-channel-not-found outcome=skipped",
                    RouteLogEvents.OPERATION_ROUTE_SKIPPED,
                    gatewayName);
            return;
        }
        if (!Boolean.TRUE.equals(gatewayChannel.getActive())) {
            log.warn("event={} layer=operation gatewayName={} reason=inactive-gateway-channel outcome=skipped",
                    RouteLogEvents.OPERATION_ROUTE_SKIPPED,
                    gatewayChannel.getName());
            return;
        }
        RuntimeTargetKind targetKind = runtimeRouteActivation.resolveTargetKind(gatewayChannel);
        if (runtimeTarget.targetKind() != targetKind) {
            log.warn("event={} layer=operation gatewayName={} configuredTargetKind={} resolvedTargetKind={} reason=target-kind-mismatch outcome=skipped",
                    RouteLogEvents.OPERATION_ROUTE_SKIPPED,
                    gatewayChannel.getName(),
                    runtimeTarget.targetKind(),
                    targetKind);
            return;
        }

        RuntimeRoutePlan routePlan = runtimeRoutePlanProvider.provide(gatewayChannel);
        routePlan.servicePlans()
                .stream()
                .flatMap(this::serviceOperations)
                .filter(serviceOperation -> Boolean.TRUE.equals(serviceOperation.getActive()))
                .map(ServiceOperation::getOperationName)
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .forEach(requiredOperationNames::add);
    }

    private java.util.stream.Stream<ServiceOperation> serviceOperations(RuntimeServicePlan servicePlan) {
        if (servicePlan == null || servicePlan.service() == null
                || servicePlan.service().getServiceOperations() == null) {
            return java.util.stream.Stream.empty();
        }
        return servicePlan.service().getServiceOperations().stream();
    }

    private void buildOperationRoute(Operation operation) {
        String routeId = RouteIdSupport.operationRouteId(operation.getName());
        String fromUri = "direct:" + routeId;
        RouteDefinition route = from(fromUri)
                .routeId(routeId)
                .setProperty(Message.OPERATION, constant(operation));

        defineExceptionHandler(route, operation);
        applyMetrics(route, operation);

        List<PluginDetail> orderedBeforePluginDetails = pluginResolverService.resolveOrderedPluginDetails(operation, PluginPhase.BEFORE);
        applyBeforePlugins(route, orderedBeforePluginDetails, Map.of(Message.OPERATION, operation));
        buildTarget(route, operation);
        List<PluginDetail> orderedAfterPluginDetails = pluginResolverService.resolveOrderedPluginDetails(operation, PluginPhase.AFTER);
        applyAfterPlugins(route, orderedAfterPluginDetails, Map.of(Message.OPERATION, operation));
        applyMetricsSuccess(route, operation);

        log.info("event={} layer=operation operationName={} operationType={} routeId={} fromUri={} outcome=success",
                RouteLogEvents.OPERATION_ROUTE_REGISTERED,
                operation.getName(),
                operation.getType(),
                routeId,
                fromUri);
    }

    private void logMissingOperations(Set<String> requiredOperationNames, Set<String> builtOperationNames) {
        requiredOperationNames.stream()
                .filter(operationName -> !builtOperationNames.contains(operationName))
                .forEach(operationName -> log.warn(
                        "event={} layer=operation operationName={} reason=required-operation-not-active-or-not-found outcome=skipped",
                        RouteLogEvents.OPERATION_ROUTE_SKIPPED,
                        operationName));
    }

    private Map<String, Operation> activeOperationsByRequiredName(Set<String> requiredOperationNames,
                                                                  List<Operation> operations) {
        Map<String, Operation> operationsByName = new LinkedHashMap<>();
        if (operations == null) {
            return operationsByName;
        }
        for (Operation operation : operations) {
            if (operation == null || !Boolean.TRUE.equals(operation.getActive())) {
                continue;
            }
            String operationName = StringUtils.trimToNull(operation.getName());
            if (operationName == null) {
                throw new IllegalStateException("Operation lookup returned an active operation without a name.");
            }
            if (!requiredOperationNames.contains(operationName)) {
                throw new IllegalStateException("Operation lookup returned unexpected active operation '"
                        + operationName + "' that was not required by active runtime service plans.");
            }
            Operation existing = operationsByName.putIfAbsent(operationName, operation);
            if (existing != null) {
                throw new IllegalStateException("Duplicate active operation returned for required operation name '"
                        + operationName + "'.");
            }
        }
        return operationsByName;
    }

    private void defineExceptionHandler(RouteDefinition route, Operation operation) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String routeId = exchange.getFromRouteId();
                    observationTraceSupport.finishOperationCallFailure(exchange, operation, exception);
                    recordOperationMetrics(exchange, operation, exception);
                    observationTraceSupport.traceException(exchange, exception);
                    log.error("[Error Handler] Route {} threw: {}", routeId, exception.getMessage(), exception);
                    if (Boolean.TRUE.equals(exchange.getProperty(Message.SERVICE_LAYER_INVOCATION, Boolean.class))) {
                        // CMNEW-119: service-layer direct calls must receive SCMFault, not protocol-specific gateway output.
                        globalErrorHandler.handle(exchange);
                    } else {
                        exchange.getIn().setBody(exception);
                    }
                })
//                .filter(exchange -> !Boolean.TRUE.equals(exchange.getProperty(Message.SERVICE_LAYER_INVOCATION, Boolean.class)))
                .to(Routes.GLOBAL_ERROR_HANDLER)
                .end();
    }

    private void applyMetrics(RouteDefinition route, Operation operation) {
        route.process(exchange -> exchange.setProperty(RouteLogSupport.OPERATION_START_NANOS, System.nanoTime()));
    }

    private void applyMetricsSuccess(RouteDefinition route, Operation operation) {
        route.process(exchange -> recordOperationMetrics(exchange, operation, null));
    }

    private void applyBeforePlugins(RouteDefinition route, List<PluginDetail> orderedBeforePluginDetails, Map<String, ?> properties) {
        if (orderedBeforePluginDetails == null) {
            return;
        }

        orderedBeforePluginDetails.forEach(detail -> {
            PluginHandler handler = resolvePluginHandler(detail);
            handler.init(route, detail, properties);
            route.process(exchange -> pluginObservationSupport.execute(exchange, detail, handler, "operation", () -> handler.handle(exchange, detail)));
        });
    }

    private void buildTarget(RouteDefinition route, Operation operation) {
        OperationTypeHandler handler = operationTypeHandlers.stream()
                .filter(h -> Objects.equals(operation.getType(), h.getOperationType()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Operation type handler not found for operation " + operation.getName()));
        handler.internalConfig(route, operation, observationTraceSupport);
    }

    private void applyAfterPlugins(RouteDefinition route, List<PluginDetail> orderedAfterPluginDetails, Map<String, ?> properties) {
        if (orderedAfterPluginDetails == null) {
            return;
        }

        orderedAfterPluginDetails.forEach(detail -> {
            PluginHandler handler = resolvePluginHandler(detail);
            handler.init(route, detail, properties);
            route.process(exchange -> pluginObservationSupport.execute(exchange, detail, handler, "operation", () -> handler.handle(exchange, detail)));
        });
    }

    private void recordOperationMetrics(Exchange exchange, Operation operation, Exception exception) {
        ScmObservation observation = observationProvider.getIfAvailable();
        if (observation == null) {
            return;
        }
        String outcome = exception == null ? "success" : "failure";
        long durationNanos = operationDurationNanos(exchange);
        Map<String, String> fields = exchangeMdc.fields(exchange);
        ObservationContext context = observationContextProvider.getIfAvailable();

        MetricCounterBuilder calls = observation.metric().counter(CommonMetricNames.OPERATION_CALLS);
        tagOperation(calls, context, fields, operation, outcome, exception);
        calls.increment();

        MetricTimerBuilder duration = observation.metric().timer(CommonMetricNames.OPERATION_DURATION);
        tagOperation(duration, context, fields, operation, outcome, exception);
        duration.record(durationNanos, TimeUnit.NANOSECONDS);

        if (exception != null) {
            MetricCounterBuilder faults = observation.metric().counter(CommonMetricNames.FAULTS);
            tagOperation(faults, context, fields, operation, outcome, exception);
            faults.increment();
        }
    }

    private void tagOperation(
            MetricCounterBuilder builder,
            ObservationContext context,
            Map<String, String> fields,
            Operation operation,
            String outcome,
            Exception exception
    ) {
        builder.tag(CoreMetricTags.APP_NAME, context != null ? context.appName() : null)
                .tag(CoreMetricTags.APP_PROFILE, context != null ? context.appProfile() : null)
                .tag(CoreMetricTags.GATEWAY_NAME, fields.get("gatewayName"))
                .tag(CommonMetricTags.CHANNEL_CODE, fields.get("channelCode"))
                .tag(CoreMetricTags.SERVICE_CODE, fields.get("serviceCode"))
                .tag(CommonMetricTags.OPERATION_CODE, operation != null ? operation.getName() : fields.get("operationName"))
                .tag("operation_name", operation != null ? operation.getName() : fields.get("operationName"))
                .tag("operation_type", operation != null && operation.getType() != null ? operation.getType().name() : null)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag(CommonMetricTags.ERROR_CODE, exception != null ? exception.getClass().getSimpleName() : null);
    }

    private void tagOperation(
            MetricTimerBuilder builder,
            ObservationContext context,
            Map<String, String> fields,
            Operation operation,
            String outcome,
            Exception exception
    ) {
        builder.tag(CoreMetricTags.APP_NAME, context != null ? context.appName() : null)
                .tag(CoreMetricTags.APP_PROFILE, context != null ? context.appProfile() : null)
                .tag(CoreMetricTags.GATEWAY_NAME, fields.get("gatewayName"))
                .tag(CommonMetricTags.CHANNEL_CODE, fields.get("channelCode"))
                .tag(CoreMetricTags.SERVICE_CODE, fields.get("serviceCode"))
                .tag(CommonMetricTags.OPERATION_CODE, operation != null ? operation.getName() : fields.get("operationName"))
                .tag("operation_name", operation != null ? operation.getName() : fields.get("operationName"))
                .tag("operation_type", operation != null && operation.getType() != null ? operation.getType().name() : null)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .tag(CommonMetricTags.ERROR_CODE, exception != null ? exception.getClass().getSimpleName() : null);
    }

    private long operationDurationNanos(Exchange exchange) {
        Long startNanos = exchange.getProperty(RouteLogSupport.OPERATION_START_NANOS, Long.class);
        return startNanos != null ? Math.max(0L, System.nanoTime() - startNanos) : 0L;
    }

    private PluginHandler resolvePluginHandler(PluginDetail detail) {
        PluginHandler handler = pluginHandlers.get(detail.getName());
        if (handler == null) {
            throw new IllegalArgumentException("Plugin handler not found: " + detail.getName());
        }
        return handler;
    }
}
