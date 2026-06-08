package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.core.integration.observability.RouteLogEvents;
import ir.daneshrefah.scm.core.integration.observability.RouteLogSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeMode;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayChannelLayerRouteBuilder extends RouteBuilder {
    private final GatewayService gatewayService;
    private final RuntimeRoutePlanProvider runtimeRoutePlanProvider;
    private final List<GatewayInboundRouteFactory> inboundRouteFactories;
    private final GatewayRoutePipelineConfigurer gatewayRoutePipelineConfigurer;
    private final RuntimeRouteActivation runtimeRouteActivation;

    @Override
    public void configure() {
        RuntimeMode runtimeMode = runtimeRouteActivation.runtimeMode();
        List<RuntimeTargetProperties> runtimeTargets = runtimeRouteActivation.runtimeTargets();
        log.info("event={} layer=gateway runtimeMode={} targetCount={} outcome=started",
                RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_STARTED, runtimeMode, runtimeTargets.size());
        if (CollectionUtils.isEmpty(inboundRouteFactories)) {
            log.error("event={} layer=gateway runtimeMode={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_FAILED,
                    runtimeMode,
                    IllegalStateException.class.getSimpleName(),
                    "No gateway inbound route factory found");
            throw new IllegalStateException("No gateway inbound route factory found");
        }
        runtimeTargets.forEach(runtimeTarget ->
                runtimeTarget.gatewayNames().forEach(gatewayName ->
                        configureGatewayTarget(runtimeMode, runtimeTarget, gatewayName)));
        log.info("event={} layer=gateway runtimeMode={} targetCount={} outcome=success",
                RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_COMPLETED, runtimeMode, runtimeTargets.size());
    }

    private void configureGatewayTarget(RuntimeMode runtimeMode,
                                        RuntimeTargetProperties runtimeTarget,
                                        String gatewayName) {
        long startNanos = System.nanoTime();
        log.info("event={} layer=gateway gatewayName={} runtimeMode={} configuredTargetKind={} outcome=started",
                RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_STARTED,
                gatewayName,
                runtimeMode,
                runtimeTarget.targetKind());
        try {
            configureGatewayTargetSafely(runtimeMode, runtimeTarget, gatewayName, startNanos);
        } catch (RuntimeException exception) {
            log.error("event={} layer=gateway gatewayName={} runtimeMode={} configuredTargetKind={} durationMs={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_FAILED,
                    gatewayName,
                    runtimeMode,
                    runtimeTarget.targetKind(),
                    RouteLogSupport.elapsedMs(startNanos),
                    RouteLogSupport.failureType(exception),
                    RouteLogSupport.failureMessage(exception),
                    exception);
            throw exception;
        }
    }

    private void configureGatewayTargetSafely(RuntimeMode runtimeMode,
                                              RuntimeTargetProperties runtimeTarget,
                                              String gatewayName,
                                              long startNanos) {
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(gatewayName);
        if (gatewayChannel == null) {
            throw new IllegalStateException("Gateway channel '" + gatewayName + "' not found");
        }
        log.info("event={} layer=gateway gatewayName={} runtimeMode={} protocol={} outcome=success",
                RouteLogEvents.GATEWAY_CHANNEL_RESOLVED,
                gatewayChannel.getName(),
                runtimeMode,
                gatewayChannel.getProtocolType());
        RuntimeTargetKind targetKind = runtimeRouteActivation.resolveTargetKind(gatewayChannel);
        validateConfiguredTargetKind(runtimeTarget, gatewayChannel, targetKind);
        log.info("event={} layer=gateway gatewayName={} runtimeMode={} targetKind={} protocol={} outcome=success",
                RouteLogEvents.GATEWAY_TARGET_KIND_RESOLVED,
                gatewayChannel.getName(),
                runtimeMode,
                targetKind,
                gatewayChannel.getProtocolType());
        if (!runtimeRouteActivation.shouldBuildGatewayRoutes(runtimeMode, targetKind)) {
            log.info("event={} layer=gateway gatewayName={} runtimeMode={} targetKind={} protocol={} durationMs={} outcome=skipped reason=runtime-mode",
                    RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_SKIPPED,
                    gatewayChannel.getName(),
                    runtimeMode,
                    targetKind,
                    gatewayChannel.getProtocolType(),
                    RouteLogSupport.elapsedMs(startNanos));
            return;
        }

        RuntimeRoutePlan routePlan = resolveRoutePlan(gatewayChannel, runtimeMode);
        log.info("event={} layer=gateway gatewayName={} runtimeMode={} protocol={} targetKind={} serviceCount={} outcome=success",
                RouteLogEvents.GATEWAY_ROUTE_PLAN_RESOLVED,
                gatewayChannel.getName(),
                runtimeMode,
                gatewayChannel.getProtocolType(),
                routePlan.targetKind(),
                routePlan.servicePlans().size());
        GatewayInboundRouteFactory inboundRouteFactory = inboundRouteFactories.stream()
                .filter(factory -> Objects.equals(gatewayChannel.getProtocolType(), factory.protocol()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No inbound route factory for "
                        + gatewayChannel.getName()
                        + " gateway with " + gatewayChannel.getProtocolType() + " protocol"));

        log.info("event={} layer=gateway gatewayName={} runtimeMode={} targetKind={} protocol={} factory={} outcome=success",
                RouteLogEvents.GATEWAY_INBOUND_ROUTE_FACTORY_RESOLVED,
                gatewayChannel.getName(),
                runtimeMode,
                routePlan.targetKind(),
                gatewayChannel.getProtocolType(),
                inboundRouteFactory.getClass().getSimpleName());
        routePlan.servicePlans().forEach(servicePlan ->
                inboundRouteFactory.createRoutes(new GatewayInboundRouteContext(
                                gatewayChannel,
                                routePlan,
                                servicePlan,
                                this))
                        .forEach(inboundRoute -> configureGatewayRoute(routePlan, servicePlan, inboundRoute)));
        log.info("event={} layer=gateway gatewayName={} runtimeMode={} targetKind={} protocol={} serviceCount={} durationMs={} outcome=success",
                RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_COMPLETED,
                gatewayChannel.getName(),
                runtimeMode,
                routePlan.targetKind(),
                gatewayChannel.getProtocolType(),
                routePlan.servicePlans().size(),
                RouteLogSupport.elapsedMs(startNanos));
    }

    private RuntimeRoutePlan resolveRoutePlan(GatewayChannel gatewayChannel, RuntimeMode runtimeMode) {
        try {
            return runtimeRoutePlanProvider.provide(gatewayChannel);
        } catch (RuntimeException exception) {
            log.error("event={} layer=gateway gatewayName={} runtimeMode={} protocol={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_FAILED,
                    gatewayChannel.getName(),
                    runtimeMode,
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

    private void configureGatewayRoute(RuntimeRoutePlan routePlan,
                                       RuntimeServicePlan servicePlan,
                                       InboundRouteDefinition inboundRoute) {
        try {
            gatewayRoutePipelineConfigurer.configureGatewayRoute(new ChannelRouteBuildContext(routePlan, servicePlan), inboundRoute);
        } catch (RuntimeException exception) {
            log.error("event={} layer=gateway routeId={} gatewayName={} targetKind={} serviceCode={} serviceVersion={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.GATEWAY_ROUTE_CONSTRUCTION_FAILED,
                    inboundRoute.route().getRouteId(),
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.service().getCode(),
                    inboundRoute.serviceVersion(),
                    RouteLogSupport.failureType(exception),
                    RouteLogSupport.failureMessage(exception),
                    exception);
            throw exception;
        }
    }
}
