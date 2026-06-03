package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.audit.ServiceAuditEventPublisher;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeMode;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.runtime.ScmRuntimeProperties;
import ir.daneshrefah.scm.core.integration.service.guard.ChannelServiceAccessGuard;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import ir.daneshrefah.scm.core.integration.service.guard.RuntimeChannelGuard;
import ir.daneshrefah.scm.core.integration.service.metrics.ServicePluginMetrics;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceLayerRouteBuilder extends RouteBuilder {
    private static final String SERVICE_START_NANOS = "scmServiceStartNanos";

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
    private final ScmRuntimeProperties scmRuntimeProperties;
    private final RuntimeRouteActivation runtimeRouteActivation;

    @Override
    public void configure() {
        String name = scmRuntimeProperties.gatewayName();
        RuntimeMode runtimeMode = runtimeRouteActivation.runtimeMode();
        log.info("Service route construction started gatewayName={} runtimeMode={}", name, runtimeMode);
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(name);
        if (gatewayChannel == null) {
            log.error("Service route construction failed gateway channel not found gatewayName={}", name);
            throw new IllegalStateException("Gateway channel '" + name + "' not found");
        }
        RuntimeTargetKind targetKind = runtimeRouteActivation.resolveTargetKind(gatewayChannel);
        if (!runtimeRouteActivation.shouldBuildServiceRoutes(runtimeMode, targetKind)) {
            log.warn("Service route construction skipped gatewayName={} runtimeMode={} targetKind={}",
                    gatewayChannel.getName(), runtimeMode, targetKind);
            return;
        }

        RuntimeRoutePlan routePlan = resolveRoutePlan(gatewayChannel, runtimeMode);
        log.info("Service route plan resolved gatewayName={} protocol={} targetKind={} serviceCount={}",
                gatewayChannel.getName(), gatewayChannel.getProtocolType(), routePlan.targetKind(), routePlan.servicePlans().size());
        if (routePlan.servicePlans().isEmpty()) {
            log.warn("Service route construction skipped because route plan has no services gatewayName={}",
                    gatewayChannel.getName());
        }
        List<PluginDetail> channelPluginDetails = pluginResolverService.resolveOrderedPluginDetails(gatewayChannel.getChannel());
        routePlan.servicePlans().forEach(servicePlan -> {
            try {
                buildServiceRoute(routePlan, servicePlan, channelPluginDetails);
            } catch (RuntimeException exception) {
                log.error("Service route construction failed gatewayName={} serviceCode={}",
                        gatewayChannel.getName(), servicePlan.service().getCode(), exception);
                throw exception;
            }
        });
        log.info("Service route construction completed gatewayName={} serviceCount={}",
                gatewayChannel.getName(), routePlan.servicePlans().size());
    }

    private RuntimeRoutePlan resolveRoutePlan(GatewayChannel gatewayChannel, RuntimeMode runtimeMode) {
        try {
            return runtimeRoutePlanProvider.provide(gatewayChannel);
        } catch (RuntimeException exception) {
            log.error("Service route planning failed gatewayName={} runtimeMode={} protocol={}",
                    gatewayChannel.getName(), runtimeMode, gatewayChannel.getProtocolType(), exception);
            throw exception;
        }
    }

    private void buildServiceRoute(RuntimeRoutePlan routePlan,
                                   RuntimeServicePlan servicePlan,
                                   List<PluginDetail> channelPluginDetails) {
        Service service = servicePlan.service();
        String serviceUri = serviceRouteUriResolver.resolve(service);
        String routeId = "scm-service-" + serviceRouteUriResolver.normalizeServiceCode(service.getCode());
        log.info("Service route registration started routeId={} gatewayName={} channelCode={} serviceCode={} targetUri={}",
                routeId,
                servicePlan.gatewayChannel().getName(),
                channelCode(servicePlan.channelServiceAccess()),
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

        route.process(exchange -> log.info("Target routing started gatewayName={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={}",
                servicePlan.gatewayChannel().getName(),
                channelCode(exchange, servicePlan),
                service.getCode(),
                serviceVersion(exchange),
                exchange.getFromRouteId(),
                exchange.getExchangeId()));
        serviceTargetRouter.buildTarget(route, service);
        route.process(exchange -> log.info("Target routing finished gatewayName={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={}",
                servicePlan.gatewayChannel().getName(),
                channelCode(exchange, servicePlan),
                service.getCode(),
                serviceVersion(exchange),
                exchange.getFromRouteId(),
                exchange.getExchangeId()));

        List<PluginDetail> afterPlugins = pluginResolverService.resolveOrderedPluginDetails(
                channelPluginDetails,
                service,
                PluginPhase.AFTER);
        applyPlugins(route, afterPlugins, servicePlan);
        applyServiceSuccess(route, servicePlan);
        log.info("Service route registered routeId={} gatewayName={} channelCode={} serviceCode={} targetUri={}",
                routeId,
                servicePlan.gatewayChannel().getName(),
                channelCode(servicePlan.channelServiceAccess()),
                service.getCode(),
                serviceUri);
    }

    private void applyServiceStart(ProcessorDefinition<?> route, RuntimeServicePlan servicePlan) {
        route.process(exchange -> {
            exchange.setProperty(SERVICE_START_NANOS, System.nanoTime());
            scmExchangeMdc.put(exchange);
            runtimeChannelGuard.check(exchange, servicePlan);
            channelServiceAccessGuard.check(exchange, servicePlan);
            log.info("Service route started gatewayName={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
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
        try {
            log.info("Service plugin started gatewayName={} channelCode={} serviceCode={} serviceVersion={} pluginName={} pluginPhase={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    detail.getName(),
                    detail.getPhase(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
            handler.handle(exchange, detail);
            servicePluginMetrics.recordPluginExecution(
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    System.nanoTime() - startNanos,
                    true);
            log.info("Service plugin finished gatewayName={} channelCode={} serviceCode={} serviceVersion={} pluginName={} pluginPhase={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    detail.getName(),
                    detail.getPhase(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
        } catch (Exception e) {
            servicePluginMetrics.recordPluginExecution(
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    System.nanoTime() - startNanos,
                    false);
            log.warn("Service plugin failed gatewayName={} channelCode={} serviceCode={} serviceVersion={} pluginName={} pluginPhase={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    detail.getName(),
                    detail.getPhase(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
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
            serviceAuditEventPublisher.recordSuccess(exchange);
            log.info("Service route succeeded gatewayName={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    channelCode(exchange, servicePlan),
                    servicePlan.service().getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
        });
    }

    private void defineExceptionHandler(RouteDefinition route, RuntimeServicePlan servicePlan) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    scmExchangeMdc.put(exchange);
                    TraceUtils traceUtils = TraceUtils.getInstance();
                    if (traceUtils != null) {
                        traceUtils.traceException(exchange, exception);
                    }
                    globalErrorHandler.handle(exchange);
                    servicePluginMetrics.recordServiceExecution(
                            servicePlan.gatewayChannel().getName(),
                            channelCode(exchange, servicePlan),
                            servicePlan.service().getCode(),
                            exchange.getProperty(Message.OPERATION_NAME, String.class),
                            serviceDuration(exchange),
                            false);
                    serviceAuditEventPublisher.recordFailure(exchange, exception);
                    log.warn("Service route failed gatewayName={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={}",
                            servicePlan.gatewayChannel().getName(),
                            channelCode(exchange, servicePlan),
                            servicePlan.service().getCode(),
                            serviceVersion(exchange),
                            exchange.getFromRouteId(),
                            exchange.getExchangeId(),
                            exception);
                });
    }

    private long serviceDuration(Exchange exchange) {
        Long startNanos = exchange.getProperty(SERVICE_START_NANOS, Long.class);
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

    private PluginHandler resolvePluginHandler(PluginDetail detail) {
        PluginHandler handler = pluginHandlers.get(detail.getName());
        if (handler == null) {
            throw new IllegalArgumentException("Plugin handler not found: " + detail.getName());
        }
        return handler;
    }
}
