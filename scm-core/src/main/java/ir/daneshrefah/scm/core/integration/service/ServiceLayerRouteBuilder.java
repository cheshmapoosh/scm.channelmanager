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
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.guard.ChannelServiceAccessGuard;
import ir.daneshrefah.scm.core.integration.service.guard.RuntimeChannelGuard;
import ir.daneshrefah.scm.core.integration.service.metrics.ServicePluginMetrics;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${scm.app-name}")
    private String name;

    @Override
    public void configure() {
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(name);
        if (gatewayChannel == null) {
            throw new IllegalStateException("Gateway channel '" + name + "' not found");
        }

        RuntimeRoutePlan routePlan = runtimeRoutePlanProvider.provide(gatewayChannel);
        List<PluginDetail> channelPluginDetails = pluginResolverService.resolveOrderedPluginDetails(gatewayChannel.getChannel());
        routePlan.servicePlans().forEach(servicePlan -> buildServiceRoute(routePlan, servicePlan, channelPluginDetails));
    }

    private void buildServiceRoute(RuntimeRoutePlan routePlan,
                                   RuntimeServicePlan servicePlan,
                                   List<PluginDetail> channelPluginDetails) {
        Service service = servicePlan.service();
        String serviceUri = serviceRouteUriResolver.resolve(service);
        String routeId = "scm-service-" + serviceRouteUriResolver.normalizeServiceCode(service.getCode());
        RouteDefinition route = from(serviceUri)
                .routeId(routeId)
                .setProperty(Message.RUNTIME_ROUTE_PLAN, constant(routePlan))
                .setProperty(Message.RUNTIME_SERVICE_PLAN, constant(servicePlan))
                .setProperty(Message.SERVICE, constant(service))
                .setProperty(Message.CHANNEL_CODE, constant(servicePlan.channelServiceAccess().getChannel().getCode()))
                .setProperty(Message.CHANNEL_SERVICE_ACCESS, constant(servicePlan.channelServiceAccess()))
                .setProperty(Message.GATEWAY_CHANNEL, constant(servicePlan.gatewayChannel()))
                .setProperty(Message.GATEWAY_NAME, constant(servicePlan.gatewayChannel().getName()))
                .setProperty(Message.GATEWAY_CHANNEL_PROTOCOL, constant(servicePlan.gatewayChannel().getProtocolType()))
                .setProperty(Message.SERVICE_LAYER_INVOCATION, constant(true));

        defineExceptionHandler(route, servicePlan);
        applyServiceStart(route, servicePlan);

        List<PluginDetail> beforePlugins = pluginResolverService.resolveOrderedPluginDetails(
                channelPluginDetails,
                service,
                PluginPhase.BEFORE);
        applyPlugins(route, beforePlugins, servicePlan);

        route.process(exchange -> log.info("Target routing started gatewayName={} channelCode={} serviceCode={} routeId={} exchangeId={}",
                servicePlan.gatewayChannel().getName(),
                servicePlan.channelServiceAccess().getChannel().getCode(),
                service.getCode(),
                exchange.getFromRouteId(),
                exchange.getExchangeId()));
        serviceTargetRouter.buildTarget(route, service);
        route.process(exchange -> log.info("Target routing finished gatewayName={} channelCode={} serviceCode={} routeId={} exchangeId={}",
                servicePlan.gatewayChannel().getName(),
                servicePlan.channelServiceAccess().getChannel().getCode(),
                service.getCode(),
                exchange.getFromRouteId(),
                exchange.getExchangeId()));

        List<PluginDetail> afterPlugins = pluginResolverService.resolveOrderedPluginDetails(
                channelPluginDetails,
                service,
                PluginPhase.AFTER);
        applyPlugins(route, afterPlugins, servicePlan);
        applyServiceSuccess(route, servicePlan);
    }

    private void applyServiceStart(ProcessorDefinition<?> route, RuntimeServicePlan servicePlan) {
        route.process(exchange -> {
            exchange.setProperty(SERVICE_START_NANOS, System.nanoTime());
            scmExchangeMdc.put(exchange);
            runtimeChannelGuard.check(exchange, servicePlan);
            channelServiceAccessGuard.check(exchange, servicePlan);
            log.info("Service route started gatewayName={} channelCode={} serviceCode={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
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
            log.info("Service plugin started gatewayName={} channelCode={} serviceCode={} pluginName={} pluginPhase={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
                    detail.getName(),
                    detail.getPhase(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
            handler.handle(exchange, detail);
            servicePluginMetrics.recordPluginExecution(
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    System.nanoTime() - startNanos,
                    true);
            log.info("Service plugin finished gatewayName={} channelCode={} serviceCode={} pluginName={} pluginPhase={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
                    detail.getName(),
                    detail.getPhase(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
        } catch (Exception e) {
            servicePluginMetrics.recordPluginExecution(
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
                    operationName,
                    detail.getName(),
                    detail.getPhase(),
                    System.nanoTime() - startNanos,
                    false);
            log.warn("Service plugin failed gatewayName={} channelCode={} serviceCode={} pluginName={} pluginPhase={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
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
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
                    exchange.getProperty(Message.OPERATION_NAME, String.class),
                    durationNanos,
                    true);
            log.info("Service route succeeded gatewayName={} channelCode={} serviceCode={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.channelServiceAccess().getChannel().getCode(),
                    servicePlan.service().getCode(),
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
                            servicePlan.channelServiceAccess().getChannel().getCode(),
                            servicePlan.service().getCode(),
                            exchange.getProperty(Message.OPERATION_NAME, String.class),
                            serviceDuration(exchange),
                            false);
                    log.warn("Service route failed gatewayName={} channelCode={} serviceCode={} routeId={} exchangeId={}",
                            servicePlan.gatewayChannel().getName(),
                            servicePlan.channelServiceAccess().getChannel().getCode(),
                            servicePlan.service().getCode(),
                            exchange.getFromRouteId(),
                            exchange.getExchangeId(),
                            exception);
                });
    }

    private long serviceDuration(Exchange exchange) {
        Long startNanos = exchange.getProperty(SERVICE_START_NANOS, Long.class);
        return startNanos != null ? System.nanoTime() - startNanos : 0L;
    }

    private PluginHandler resolvePluginHandler(PluginDetail detail) {
        PluginHandler handler = pluginHandlers.get(detail.getName());
        if (handler == null) {
            throw new IllegalArgumentException("Plugin handler not found: " + detail.getName());
        }
        return handler;
    }
}
