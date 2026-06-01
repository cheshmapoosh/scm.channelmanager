package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContract;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.RequestContractDecoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayChannelRouteBuilder extends RouteBuilder {
    private final GatewayService gatewayService;
    private final RuntimeRoutePlanProvider runtimeRoutePlanProvider;
    private final List<ProtocolHandler> protocolHandlers;
    private final ClientContractResolver clientContractResolver;
    private final Map<String, RequestContractDecoder> requestContractDecoders;
    private final ServiceRouteUriResolver serviceRouteUriResolver;
    private final ScmExchangeMdc scmExchangeMdc;

    @Value("${scm.app-name}")
    private String name;

    @Override
    public void configure() {
        if (CollectionUtils.isEmpty(protocolHandlers)) {
            log.error("No protocol handler found");
            throw new IllegalStateException("No protocol handler found");
        }
        GatewayChannel gatewayChannel = gatewayService.findGatewayChannelByName(name);
        if (gatewayChannel == null) {
            log.error("Gateway channel '{}' not found", name);
            throw new IllegalStateException("Gateway channel '" + name + "' not found");
        }

        RuntimeRoutePlan routePlan = runtimeRoutePlanProvider.provide(gatewayChannel);
        ProtocolHandler protocolHandler = protocolHandlers.stream()
                .filter(h -> Objects.equals(gatewayChannel.getProtocolType(), h.getProtocol()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No handler for " + gatewayChannel.getName()
                                + " gateway with " + gatewayChannel.getProtocolType() + " protocol"));

        ProtocolHandler.ProtocolConfigurer protocolConfigurer = protocolHandler.config(gatewayChannel, this);
        routePlan.servicePlans().forEach(servicePlan ->
                protocolConfigurer.routeDefinition(servicePlan)
                        .forEach(inboundRoute -> configureGatewayRoute(routePlan, servicePlan, inboundRoute)));
    }

    private void configureGatewayRoute(RuntimeRoutePlan routePlan,
                                       RuntimeServicePlan servicePlan,
                                       InboundRouteDefinition inboundRoute) {
        RouteDefinition route = inboundRoute.route();
        Service service = servicePlan.service();
        ChannelServiceAccess access = servicePlan.channelServiceAccess();

        route.setProperty(Message.RUNTIME_ROUTE_PLAN, constant(routePlan));
        route.setProperty(Message.RUNTIME_SERVICE_PLAN, constant(servicePlan));
        route.setProperty(Message.SERVICE, constant(service));
        route.setProperty(Message.CHANNEL_CODE, constant(access.getChannel().getCode()));
        route.setProperty(Message.CHANNEL_SERVICE_ACCESS, constant(access));
        route.setProperty(Message.GATEWAY_CHANNEL, constant(servicePlan.gatewayChannel()));
        route.setProperty(Message.GATEWAY_NAME, constant(servicePlan.gatewayChannel().getName()));
        route.setProperty(Message.GATEWAY_CHANNEL_PROTOCOL, constant(servicePlan.gatewayChannel().getProtocolType()));
        route.setProperty(Message.CHANNEL_SERVICE_DEFINITION, constant(inboundRoute.channelServiceDefinition()));

        defineExceptionHandler(route);
        route.process(exchange -> {
            scmExchangeMdc.put(exchange);
            TraceUtils traceUtils = TraceUtils.getInstance();
            if (traceUtils != null) {
                traceUtils.traceScmRequest(exchange, service);
            }
            log.info("Gateway inbound received gatewayName={} channelCode={} serviceCode={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    access.getChannel().getCode(),
                    service.getCode(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
        });

        route.process(exchange -> {
            ClientContract contract = clientContractResolver.resolve(
                    servicePlan.gatewayChannel(),
                    inboundRoute.channelServiceDefinition());
            exchange.setProperty(Message.CLIENT_CONTRACT, contract);
            RequestContractDecoder decoder = resolveRequestDecoder(contract);
            decoder.decode(exchange, contract);
            scmExchangeMdc.put(exchange);
            log.info("Gateway request decoded gatewayName={} channelCode={} serviceCode={} contract={} routeId={} exchangeId={}",
                    servicePlan.gatewayChannel().getName(),
                    access.getChannel().getCode(),
                    service.getCode(),
                    contract.name(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId());
        });

        route.process(exchange -> log.info("Gateway dispatching to service gatewayName={} channelCode={} serviceCode={} targetUri={} routeId={} exchangeId={}",
                servicePlan.gatewayChannel().getName(),
                access.getChannel().getCode(),
                service.getCode(),
                serviceRouteUriResolver.resolve(service),
                exchange.getFromRouteId(),
                exchange.getExchangeId()));
        route.to(serviceRouteUriResolver.resolve(service));
        route.to(Routes.GLOBAL_RESPONSE_HANDLER);
    }

    private void defineExceptionHandler(RouteDefinition route) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    scmExchangeMdc.put(exchange);
                    TraceUtils traceUtils = TraceUtils.getInstance();
                    if (traceUtils != null) {
                        traceUtils.traceException(exchange, exception);
                    }
                    log.warn("Gateway route failed routeId={} exchangeId={}",
                            exchange.getFromRouteId(),
                            exchange.getExchangeId(),
                            exception);
                })
                .to(Routes.GLOBAL_ERROR_HANDLER);
    }

    private RequestContractDecoder resolveRequestDecoder(ClientContract contract) {
        RequestContractDecoder decoder = requestContractDecoders.get(contract.requestDecoder());
        if (decoder == null) {
            throw new IllegalStateException("Request decoder not found: " + contract.requestDecoder());
        }
        return decoder;
    }
}
