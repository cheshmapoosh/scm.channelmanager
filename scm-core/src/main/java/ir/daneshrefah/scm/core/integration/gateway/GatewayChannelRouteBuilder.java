package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.ScmRuntimeProperties;
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
public class GatewayChannelRouteBuilder extends RouteBuilder {
    private final GatewayService gatewayService;
    private final RuntimeRoutePlanProvider runtimeRoutePlanProvider;
    private final List<ProtocolHandler> protocolHandlers;
    private final ScmRuntimeProperties scmRuntimeProperties;
    private final GatewayLayerRouteBuilder gatewayLayerRouteBuilder;

    @Override
    public void configure() {
        log.info("Gateway route construction started gatewayName={}", scmRuntimeProperties.gatewayName());
        if (CollectionUtils.isEmpty(protocolHandlers)) {
            log.error("No protocol handler found");
            throw new IllegalStateException("No protocol handler found");
        }
        String name = scmRuntimeProperties.gatewayName();
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(name);
        if (gatewayChannel == null) {
            log.error("Gateway channel '{}' not found", name);
            throw new IllegalStateException("Gateway channel '" + name + "' not found");
        }

        RuntimeRoutePlan routePlan = runtimeRoutePlanProvider.provide(gatewayChannel);
        log.info("Gateway route plan resolved gatewayName={} protocol={} targetKind={} serviceCount={}",
                gatewayChannel.getName(), gatewayChannel.getProtocolType(), routePlan.targetKind(), routePlan.servicePlans().size());
        ProtocolHandler protocolHandler = protocolHandlers.stream()
                .filter(h -> Objects.equals(gatewayChannel.getProtocolType(), h.getProtocol()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No protocol handler found gatewayName={} protocol={}",
                            gatewayChannel.getName(), gatewayChannel.getProtocolType());
                    return new IllegalStateException("No handler for " + gatewayChannel.getName()
                            + " gateway with " + gatewayChannel.getProtocolType() + " protocol");
                });

        ProtocolHandler.ProtocolConfigurer protocolConfigurer = protocolHandler.config(gatewayChannel, this);
        log.info("Gateway route construction using protocol handler gatewayName={} protocol={} handler={}",
                gatewayChannel.getName(), gatewayChannel.getProtocolType(), protocolHandler.getClass().getSimpleName());
        routePlan.servicePlans().forEach(servicePlan ->
                protocolConfigurer.routeDefinition(servicePlan)
                        .forEach(inboundRoute -> configureGatewayRoute(routePlan, servicePlan, inboundRoute)));
        log.info("Gateway route construction completed gatewayName={} serviceCount={}",
                gatewayChannel.getName(), routePlan.servicePlans().size());
    }

    private void configureGatewayRoute(RuntimeRoutePlan routePlan,
                                       RuntimeServicePlan servicePlan,
                                       InboundRouteDefinition inboundRoute) {
        try {
            gatewayLayerRouteBuilder.configureGatewayRoute(new ChannelRouteBuildContext(routePlan, servicePlan), inboundRoute);
        } catch (RuntimeException exception) {
            log.error("Gateway route construction failed routeId={} gatewayName={} serviceCode={} serviceVersion={}",
                    inboundRoute.route().getRouteId(),
                    servicePlan.gatewayChannel().getName(),
                    servicePlan.service().getCode(),
                    inboundRoute.serviceVersion(),
                    exception);
            throw exception;
        }
    }
}
