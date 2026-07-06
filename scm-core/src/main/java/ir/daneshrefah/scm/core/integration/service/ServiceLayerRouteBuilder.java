package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.audit.ServiceAuditEventPublisher;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.observability.PluginObservationSupport;
import ir.daneshrefah.scm.core.integration.observability.RouteLogEvents;
import ir.daneshrefah.scm.core.integration.observability.RouteLogSupport;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetProperties;
import ir.daneshrefah.scm.core.integration.service.guard.ChannelServiceAccessGuard;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import ir.daneshrefah.scm.core.integration.service.guard.RuntimeChannelGuard;
import ir.daneshrefah.scm.core.integration.service.metrics.ServicePluginMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceLayerRouteBuilder extends RouteBuilder {
    private final GatewayService gatewayService;
    private final RuntimeRoutePlanProvider runtimeRoutePlanProvider;
    private final ServiceRouteUriResolver serviceRouteUriResolver;
    private final RuntimeChannelGuard runtimeChannelGuard;
    private final ChannelServiceAccessGuard channelServiceAccessGuard;
    private final PluginResolverService pluginResolverService;
    private final Map<String, PluginHandler> pluginHandlers;
    private final ServiceTargetRouter serviceTargetRouter;
    private final GlobalErrorHandler globalErrorHandler;
    private final ScmExchangeMdc scmExchangeMdc;
    private final ServicePluginMetrics servicePluginMetrics;
    private final ServiceAuditEventPublisher serviceAuditEventPublisher;
    private final IncomingChannelCodeResolver incomingChannelCodeResolver;
    private final RuntimeRouteActivation runtimeRouteActivation;
    private final CoreObservationTraceSupport observationTraceSupport;
    private final PluginObservationSupport pluginObservationSupport;

    @Override
    public void configure() {
        List<RuntimeTargetProperties> runtimeTargets = runtimeRouteActivation.runtimeTargets();
        log.info("event={} layer=service targetCount={} outcome=started",
                RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_STARTED, runtimeTargets.size());
        Set<String> serviceRouteIds = new LinkedHashSet<>();
        runtimeTargets.forEach(runtimeTarget ->
                runtimeTarget.gatewayNames().forEach(gatewayName ->
                        configureServiceTarget(runtimeTarget, gatewayName, serviceRouteIds)));
        log.info("event={} layer=service targetCount={} outcome=success",
                RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_COMPLETED, runtimeTargets.size());
    }

    private void configureServiceTarget(RuntimeTargetProperties runtimeTarget,
                                        String gatewayName,
                                        Set<String> serviceRouteIds) {
        long startNanos = System.nanoTime();
        log.info("event={} layer=service gatewayName={} configuredTargetKind={} outcome=started",
                RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_STARTED,
                gatewayName,
                runtimeTarget.targetKind());
        try {
            configureServiceTargetSafely(runtimeTarget, gatewayName, serviceRouteIds, startNanos);
        } catch (RuntimeException exception) {
            log.error("event={} layer=service gatewayName={} configuredTargetKind={} durationMs={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_FAILED,
                    gatewayName,
                    runtimeTarget.targetKind(),
                    RouteLogSupport.elapsedMs(startNanos),
                    RouteLogSupport.failureType(exception),
                    RouteLogSupport.failureMessage(exception),
                    exception);
            throw exception;
        }
    }

    private void configureServiceTargetSafely(RuntimeTargetProperties runtimeTarget,
                                              String gatewayName,
                                              Set<String> serviceRouteIds,
                                              long startNanos) {
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(gatewayName);
        if (gatewayChannel == null) {
            throw new IllegalStateException("Gateway channel '" + gatewayName + "' not found");
        }
        log.info("event={} layer=service gatewayName={} protocol={} outcome=success",
                RouteLogEvents.SERVICE_GATEWAY_CHANNEL_RESOLVED,
                gatewayChannel.getName(),
                gatewayChannel.getProtocolType());
        RuntimeTargetKind targetKind = runtimeRouteActivation.resolveTargetKind(gatewayChannel);
        validateConfiguredTargetKind(runtimeTarget, gatewayChannel, targetKind);
        log.info("event={} layer=service gatewayName={} configuredTargetKind={} resolvedTargetKind={} protocol={} outcome=success",
                RouteLogEvents.SERVICE_TARGET_KIND_RESOLVED,
                gatewayChannel.getName(),
                runtimeTarget.targetKind(),
                targetKind,
                gatewayChannel.getProtocolType());

        RuntimeRoutePlan routePlan = resolveRoutePlan(gatewayChannel);
        log.info("event={} layer=service gatewayName={} protocol={} targetKind={} serviceCount={} outcome=success",
                RouteLogEvents.SERVICE_ROUTE_PLAN_RESOLVED,
                gatewayChannel.getName(), gatewayChannel.getProtocolType(), routePlan.targetKind(), routePlan.servicePlans().size());
        if (routePlan.servicePlans().isEmpty()) {
            log.warn("event={} layer=service gatewayName={} targetKind={} protocol={} outcome=skipped reason=empty-route-plan",
                    RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_SKIPPED,
                    gatewayChannel.getName(),
                    routePlan.targetKind(),
                    gatewayChannel.getProtocolType());
        }
        List<PluginDetail> channelPluginDetails = pluginResolverService.resolveOrderedPluginDetails(gatewayChannel.getChannel());
        routePlan.servicePlans().forEach(servicePlan -> {
            try {
                buildServiceRoute(routePlan, servicePlan, channelPluginDetails, serviceRouteIds);
            } catch (RuntimeException exception) {
                log.error("event={} layer=service gatewayName={} targetKind={} serviceCode={} durationMs={} outcome=failed failureType={} failureMessage={}",
                        RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_FAILED,
                        gatewayChannel.getName(),
                        routePlan.targetKind(),
                        servicePlan.service().getCode(),
                        RouteLogSupport.elapsedMs(startNanos),
                        RouteLogSupport.failureType(exception),
                        RouteLogSupport.failureMessage(exception),
                        exception);
                throw exception;
            }
        });
        log.info("event={} layer=service gatewayName={} targetKind={} protocol={} serviceCount={} durationMs={} outcome=success",
                RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_COMPLETED,
                gatewayChannel.getName(),
                routePlan.targetKind(),
                gatewayChannel.getProtocolType(),
                routePlan.servicePlans().size(),
                RouteLogSupport.elapsedMs(startNanos));
    }

    private RuntimeRoutePlan resolveRoutePlan(GatewayChannel gatewayChannel) {
        try {
            return runtimeRoutePlanProvider.provide(gatewayChannel);
        } catch (RuntimeException exception) {
            log.error("event={} layer=service gatewayName={} protocol={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.SERVICE_ROUTE_CONSTRUCTION_FAILED,
                    gatewayChannel.getName(),
                    gatewayChannel.getProtocolType(),
                    RouteLogSupport.failureType(exception),
                    RouteLogSupport.failureMessage(exception),
                    exception);
            throw exception;
        }
    }

    private void validateConfiguredTargetKind(RuntimeTargetProperties runtimeTarget,
                                              GatewayChannel gatewayChannel,
                                              RuntimeTargetKind targetKind) {
        if (runtimeTarget.targetKind() == targetKind) {
            return;
        }
        throw new IllegalStateException("Configured runtime target " + runtimeTarget.targetKind()
                + " contains gateway '" + gatewayChannel.getName()
                + "' resolved as " + targetKind + ".");
    }

    private void buildServiceRoute(RuntimeRoutePlan routePlan,
                                   RuntimeServicePlan servicePlan,
                                   List<PluginDetail> channelPluginDetails,
                                   Set<String> serviceRouteIds) {
        Service service = servicePlan.service();
        String routeId = serviceRouteUriResolver.routeId(routePlan, servicePlan);
        validateUniqueServiceRouteId(routeId, routePlan, servicePlan, serviceRouteIds);
        String serviceUri = serviceRouteUriResolver.resolve(routePlan, servicePlan);
        log.info("event={} layer=service routeId={} gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} targetUri={} outcome=started",
                RouteLogEvents.SERVICE_ROUTE_REGISTRATION_STARTED,
                routeId,
                servicePlan.gatewayChannel().getName(),
                routePlan.targetKind(),
                servicePlan.gatewayChannel().getProtocolType(),
                channelCode(servicePlan.channelServiceAccess()),
                RouteLogSupport.channelServiceAccessId(servicePlan),
                service.getCode(),
                serviceUri);
        RouteDefinition route = from(serviceUri)
                .routeId(routeId)
                .setProperty(Message.RUNTIME_ROUTE_PLAN, constant(routePlan))
                .setProperty(Message.RUNTIME_SERVICE_PLAN, constant(servicePlan))
                .setProperty(Message.SERVICE, constant(service))
                .setProperty(Message.GATEWAY_CHANNEL, constant(servicePlan.gatewayChannel()))
                .setProperty(Message.GATEWAY_NAME, constant(servicePlan.gatewayChannel().getName()))
                .setProperty(Message.GATEWAY_CHANNEL_PROTOCOL, constant(servicePlan.gatewayChannel().getProtocolType()))
                .setProperty(Message.SERVICE_LAYER_INVOCATION, constant(true));

        defineExceptionHandler(route, servicePlan);
        route.onCompletion()
                .process(exchange -> scmExchangeMdc.clear())
                .end();
        applyServiceStart(route, servicePlan);

        List<PluginDetail> beforePlugins = pluginResolverService.resolveOrderedPluginDetails(
                channelPluginDetails,
                service,
                PluginPhase.BEFORE);
        applyPlugins(route, beforePlugins, servicePlan);

        route.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} targetUri={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.SERVICE_TARGET_ROUTING_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    operationTargetUri(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });
        serviceTargetRouter.buildTarget(route, servicePlan);
        route.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} targetUri={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.SERVICE_TARGET_ROUTING_FINISHED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    operationTargetUri(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });

        List<PluginDetail> afterPlugins = pluginResolverService.resolveOrderedPluginDetails(
                channelPluginDetails,
                service,
                PluginPhase.AFTER);
        route.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.SERVICE_RESPONSE_PROCESSING_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });
        applyPlugins(route, afterPlugins, servicePlan);
        route.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.SERVICE_RESPONSE_PROCESSING_FINISHED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });
        applyServiceSuccess(route, servicePlan);
        log.info("event={} layer=service routeId={} gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} targetUri={} outcome=success",
                RouteLogEvents.SERVICE_ROUTE_REGISTERED,
                routeId,
                servicePlan.gatewayChannel().getName(),
                routePlan.targetKind(),
                servicePlan.gatewayChannel().getProtocolType(),
                channelCode(servicePlan.channelServiceAccess()),
                RouteLogSupport.channelServiceAccessId(servicePlan),
                service.getCode(),
                serviceUri);
    }

    private void validateUniqueServiceRouteId(String routeId,
                                              RuntimeRoutePlan routePlan,
                                              RuntimeServicePlan servicePlan,
                                              Set<String> serviceRouteIds) {
        if (serviceRouteIds.add(routeId)) {
            return;
        }
//        throw new IllegalStateException("Duplicate service route id '" + routeId
//                + "' for targetKind=" + routePlan.targetKind()
//                + ", gatewayName=" + routePlan.gatewayChannel().getName()
//                + ", serviceCode=" + servicePlan.service().getCode()
//                + ". Service route identity must be unique in the active runtime plan.");
    }

    private void applyServiceStart(ProcessorDefinition<?> route, RuntimeServicePlan servicePlan) {
        route.process(exchange -> {
            exchange.setProperty(RouteLogSupport.SERVICE_START_NANOS, System.nanoTime());
            Map<String, String> fields = scmExchangeMdc.put(exchange);
            observationTraceSupport.startServiceExecution(exchange, servicePlan);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.SERVICE_REQUEST_RECEIVED,
                    servicePlan.gatewayChannel().getName(),
                    RouteLogSupport.targetKind(exchange),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            try {
                log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} outcome=started",
                        RouteLogEvents.SERVICE_GUARD_CHECK_STARTED,
                        servicePlan.gatewayChannel().getName(),
                        RouteLogSupport.targetKind(exchange),
                        servicePlan.gatewayChannel().getProtocolType(),
                        channelCode(exchange, servicePlan),
                        RouteLogSupport.channelServiceAccessId(servicePlan),
                        servicePlan.service().getCode(),
                        serviceVersion(exchange),
                        exchange.getFromRouteId(),
                        exchange.getExchangeId(),
                        fields.get("correlationId"));
                runtimeChannelGuard.check(exchange, servicePlan);
                channelServiceAccessGuard.check(exchange, servicePlan);
                log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} outcome=success",
                        RouteLogEvents.SERVICE_GUARD_CHECK_PASSED,
                        servicePlan.gatewayChannel().getName(),
                        RouteLogSupport.targetKind(exchange),
                        servicePlan.gatewayChannel().getProtocolType(),
                        channelCode(exchange, servicePlan),
                        RouteLogSupport.channelServiceAccessId(servicePlan),
                        servicePlan.service().getCode(),
                        serviceVersion(exchange),
                        exchange.getFromRouteId(),
                        exchange.getExchangeId(),
                        fields.get("correlationId"));
            } catch (RuntimeException exception) {
                log.warn("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} outcome=failed failureType={} failureMessage={}",
                        RouteLogEvents.SERVICE_GUARD_CHECK_FAILED,
                        servicePlan.gatewayChannel().getName(),
                        RouteLogSupport.targetKind(exchange),
                        servicePlan.gatewayChannel().getProtocolType(),
                        channelCode(exchange, servicePlan),
                        RouteLogSupport.channelServiceAccessId(servicePlan),
                        servicePlan.service().getCode(),
                        serviceVersion(exchange),
                        exchange.getFromRouteId(),
                        exchange.getExchangeId(),
                        fields.get("correlationId"),
                        RouteLogSupport.failureType(exception),
                        RouteLogSupport.failureMessage(exception),
                        exception);
                throw exception;
            }
        });
    }

    private void applyPlugins(RouteDefinition route,
                              List<PluginDetail> pluginDetails,
                              RuntimeServicePlan servicePlan) {
        if (pluginDetails == null) {
            return;
        }

        pluginDetails.forEach(detail -> {
            PluginHandler handler = resolvePluginHandler(detail);
            if(handler==null)
                return;
            handler.init(route, detail, Map.of(Message.SERVICE, servicePlan.service()));
            route.process(exchange -> invokePlugin(exchange, detail, handler, servicePlan));
        });
    }

    private void invokePlugin(Exchange exchange,
                              PluginDetail detail,
                              PluginHandler handler,
                              RuntimeServicePlan servicePlan) throws Exception {
        long startNanos = System.nanoTime();
        String operationName = exchange.getProperty(Message.OPERATION_NAME, String.class);
        Map<String, String> fields = scmExchangeMdc.fields(exchange);
        try {
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} pluginName={} pluginPhase={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.SERVICE_PLUGIN_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    RouteLogSupport.targetKind(exchange),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            pluginObservationSupport.execute(exchange, detail, handler, "service", () -> handler.handle(exchange, detail));
            long pluginDurationNanos = System.nanoTime() - startNanos;
            servicePluginMetrics.recordPluginExecution(
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    pluginDurationNanos,
                    true);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} pluginName={} pluginPhase={} pluginDurationMs={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.SERVICE_PLUGIN_FINISHED,
                    servicePlan.gatewayChannel().getName(),
                    RouteLogSupport.targetKind(exchange),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    pluginDurationNanos / 1_000_000L,
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        } catch (Exception e) {
            long pluginDurationNanos = System.nanoTime() - startNanos;
            servicePluginMetrics.recordPluginExecution(
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    pluginDurationNanos,
                    false);
            log.warn("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} pluginName={} pluginPhase={} pluginDurationMs={} routeId={} exchangeId={} correlationId={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.SERVICE_PLUGIN_FAILED,
                    servicePlan.gatewayChannel().getName(),
                    RouteLogSupport.targetKind(exchange),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    pluginDurationNanos / 1_000_000L,
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"),
                    RouteLogSupport.failureType(e),
                    RouteLogSupport.failureMessage(e),
                    e);
            throw e;
        }
    }

    private void applyServiceSuccess(ProcessorDefinition<?> route, RuntimeServicePlan servicePlan) {
        route.process(exchange -> {
            long durationNanos = serviceDuration(exchange);
            servicePluginMetrics.recordServiceExecution(
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    durationNanos,
                    true);
            observationTraceSupport.finishServiceExecutionSuccess(exchange);
            serviceAuditEventPublisher.recordSuccess(exchange);
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} routeId={} exchangeId={} correlationId={} durationMs={} outcome=success",
                    RouteLogEvents.SERVICE_REQUEST_SUCCEEDED,
                    servicePlan.gatewayChannel().getName(),
                    RouteLogSupport.targetKind(exchange),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    RouteLogSupport.channelServiceAccessId(servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"),
                    durationNanos / 1_000_000L);
        });
    }

    private void defineExceptionHandler(RouteDefinition route, RuntimeServicePlan servicePlan) {
        routeServiceExceptions(route, exchange -> handleServiceException(exchange, servicePlan));
    }

    static void routeServiceExceptions(RouteDefinition route, Processor errorProcessor) {
        route.onException(Exception.class)
                .handled(true)
                .process(errorProcessor)
                .to(Routes.GLOBAL_RESPONSE_HANDLER);
    }

    private void handleServiceException(Exchange exchange, RuntimeServicePlan servicePlan) {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        scmExchangeMdc.put(exchange);
        observationTraceSupport.finishServiceExecutionFailure(exchange, exception);
        observationTraceSupport.traceException(exchange, exception);
        globalErrorHandler.handle(exchange);
        servicePluginMetrics.recordServiceExecution(
                servicePlan.gatewayChannel().getName(),
                channelCode(exchange, servicePlan),
                servicePlan.service().getCode(),
                exchange.getProperty(Message.OPERATION_NAME, String.class),
                serviceDuration(exchange),
                false);
        serviceAuditEventPublisher.recordFailure(exchange, exception);
        Map<String, String> fields = scmExchangeMdc.fields(exchange);
        log.warn("event={} layer=service gatewayName={} targetKind={} protocol={} channelCode={} channelServiceAccessId={} serviceCode={} serviceVersion={} operationName={} routeId={} exchangeId={} correlationId={} durationMs={} outcome=failed failureType={} failureMessage={}",
                RouteLogEvents.SERVICE_REQUEST_FAILED,
                servicePlan.gatewayChannel().getName(),
                RouteLogSupport.targetKind(exchange),
                servicePlan.gatewayChannel().getProtocolType(),
                channelCode(exchange, servicePlan),
                RouteLogSupport.channelServiceAccessId(servicePlan),
                servicePlan.service().getCode(),
                serviceVersion(exchange),
                exchange.getProperty(Message.OPERATION_NAME, String.class),
                exchange.getFromRouteId(),
                exchange.getExchangeId(),
                fields.get("correlationId"),
                serviceDuration(exchange) / 1_000_000L,
                RouteLogSupport.failureType(exception),
                RouteLogSupport.failureMessage(exception),
                exception);
    }

    private long serviceDuration(Exchange exchange) {
        Long startNanos = exchange.getProperty(RouteLogSupport.SERVICE_START_NANOS, Long.class);
        return startNanos != null ? System.nanoTime() - startNanos : 0L;
    }

    private String channelCode(Exchange exchange, RuntimeServicePlan servicePlan) {
        ChannelServiceAccess access = exchange.getProperty(Message.CHANNEL_SERVICE_ACCESS, ChannelServiceAccess.class);
        if (access != null && access.getChannel() != null && access.getChannel().getCode() != null) {
            return access.getChannel().getCode();
        }
        return incomingChannelCodeResolver.resolve(exchange)
                .orElseGet(() -> servicePlan.channelServiceAccess() != null
                        && servicePlan.channelServiceAccess().getChannel() != null
                        ? servicePlan.channelServiceAccess().getChannel().getCode()
                        : null);
    }

    private String channelCode(ChannelServiceAccess access) {
        return access != null && access.getChannel() != null ? access.getChannel().getCode() : null;
    }

    private String serviceVersion(Exchange exchange) {
        return exchange.getProperty(Message.SERVICE_VERSION, String.class);
    }

    private String operationTargetUri(Exchange exchange) {
        String targetUri = exchange.getIn().getHeader("targetUrl", String.class);
        if (targetUri != null) {
            return targetUri;
        }
        ServiceOperation serviceOperation = exchange.getProperty(Message.SERVICE_OPERATION, ServiceOperation.class);
        if (serviceOperation == null || serviceOperation.getOperationName() == null) {
            return null;
        }
        if (serviceOperation.getOperationName().contains(":")) {
            return serviceOperation.getOperationName();
        }
        return "direct:" + RouteIdSupport.operationRouteId(serviceOperation.getOperationName());
    }

    private PluginHandler resolvePluginHandler(PluginDetail detail) {
        PluginHandler handler = pluginHandlers.get(detail.getName());
        if (handler == null) {
            log.warn("Plugin handler not found: " + detail.getName());
        }
        return handler;
    }
}
