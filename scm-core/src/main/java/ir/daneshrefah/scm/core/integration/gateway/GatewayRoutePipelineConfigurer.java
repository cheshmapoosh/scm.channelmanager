package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContract;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.RequestContractDecoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.observability.RouteLogEvents;
import ir.daneshrefah.scm.core.integration.observability.RouteLogSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayRoutePipelineConfigurer {

    private final ClientContractResolver clientContractResolver;
    private final Map<String, RequestContractDecoder> requestContractDecoders;
    private final ServiceRouteUriResolver serviceRouteUriResolver;
    private final ScmExchangeMdc scmExchangeMdc;
    private final IncomingChannelCodeResolver incomingChannelCodeResolver;
    private final CoreObservationTraceSupport observationTraceSupport;

    public void configureGatewayRoute(ChannelRouteBuildContext context,
                                      InboundRouteDefinition inboundRoute) {
        RuntimeRoutePlan routePlan = context.routePlan();
        RuntimeServicePlan servicePlan = context.servicePlan();
        RouteDefinition route = inboundRoute.route();
        ProcessorDefinition<?> pipeline = inboundRoute.pipeline();
        Service service = servicePlan.service();

        log.info("event={} layer=gateway routeId={} gatewayName={} targetKind={} channelCode={} serviceCode={} serviceVersion={} protocol={} outcome=started",
                RouteLogEvents.GATEWAY_ROUTE_REGISTRATION_STARTED,
                route.getRouteId(),
                servicePlan.gatewayChannel().getName(),
                routePlan.targetKind(),
                channelCode(servicePlan.channelServiceAccess()),
                service.getCode(),
                inboundRoute.serviceVersion(),
                servicePlan.gatewayChannel().getProtocolType());

        pipeline.setProperty(Message.RUNTIME_ROUTE_PLAN, constant(routePlan));
        pipeline.setProperty(Message.RUNTIME_SERVICE_PLAN, constant(servicePlan));
        pipeline.setProperty(Message.SERVICE, constant(service));
        pipeline.setProperty(Message.GATEWAY_CHANNEL, constant(servicePlan.gatewayChannel()));
        pipeline.setProperty(Message.GATEWAY_NAME, constant(servicePlan.gatewayChannel().getName()));
        pipeline.setProperty(Message.GATEWAY_CHANNEL_PROTOCOL, constant(servicePlan.gatewayChannel().getProtocolType()));
        pipeline.setProperty(Message.CHANNEL_SERVICE_DEFINITION, constant(inboundRoute.channelServiceDefinition()));
        pipeline.setProperty(Message.SERVICE_VERSION, constant(inboundRoute.serviceVersion()));

        defineExceptionHandler(route);
        route.onCompletion()
                .process(exchange -> scmExchangeMdc.clear())
                .end();
        pipeline.process(exchange -> {
            exchange.setProperty(RouteLogSupport.GATEWAY_START_NANOS, System.nanoTime());
            applyIncomingChannel(exchange, routePlan, servicePlan, inboundRoute);
            Map<String, String> fields = scmExchangeMdc.put(exchange);
            observationTraceSupport.traceGatewayRequest(exchange, service);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.GATEWAY_REQUEST_RECEIVED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });

        pipeline.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.put(exchange);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.GATEWAY_CONTRACT_RESOLUTION_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            ClientContract contract = clientContractResolver.resolve(
                    servicePlan.gatewayChannel(),
                    inboundRoute.channelServiceDefinition(),
                    inboundRoute.serviceVersion());
            exchange.setProperty(Message.CLIENT_CONTRACT, contract);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} contractName={} requestDecoder={} responseEncoder={} faultEncoder={} source={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.GATEWAY_CONTRACT_RESOLVED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    contract.name(),
                    contract.requestDecoder(),
                    contract.responseEncoder(),
                    contract.faultEncoder(),
                    contract.source(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            RequestContractDecoder decoder = resolveRequestDecoder(contract);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} contractName={} requestDecoder={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.GATEWAY_REQUEST_DECODE_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    contract.name(),
                    contract.requestDecoder(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            decoder.decode(exchange, contract);
            fields = scmExchangeMdc.put(exchange);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} contractName={} requestDecoder={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.GATEWAY_REQUEST_DECODED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    contract.name(),
                    contract.requestDecoder(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });

        String targetUri = serviceRouteUriResolver.resolve(routePlan, servicePlan);
        pipeline.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} targetUri={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.GATEWAY_DISPATCH_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    targetUri,
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });
        pipeline.to(targetUri);
        pipeline.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} targetUri={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.GATEWAY_DISPATCH_FINISHED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    targetUri,
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} outcome=started",
                    RouteLogEvents.GATEWAY_RESPONSE_HANDLER_STARTED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
        });
        pipeline.to(Routes.GLOBAL_RESPONSE_HANDLER);
        pipeline.process(exchange -> {
            Map<String, String> fields = scmExchangeMdc.fields(exchange);
            long durationMs = RouteLogSupport.durationMs(exchange, RouteLogSupport.GATEWAY_START_NANOS);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} durationMs={} outcome=success",
                    RouteLogEvents.GATEWAY_RESPONSE_HANDLER_FINISHED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"),
                    durationMs);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} durationMs={} outcome=success",
                    RouteLogEvents.GATEWAY_REQUEST_SUCCEEDED,
                    servicePlan.gatewayChannel().getName(),
                    routePlan.targetKind(),
                    servicePlan.gatewayChannel().getProtocolType(),
                    channelCode(exchange, servicePlan),
                    service.getCode(),
                    serviceVersion(exchange),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"),
                    durationMs);
        });
        log.info("event={} layer=gateway routeId={} gatewayName={} targetKind={} channelCode={} serviceCode={} serviceVersion={} targetUri={} outcome=success",
                RouteLogEvents.GATEWAY_ROUTE_REGISTERED,
                route.getRouteId(),
                servicePlan.gatewayChannel().getName(),
                routePlan.targetKind(),
                channelCode(servicePlan.channelServiceAccess()),
                service.getCode(),
                inboundRoute.serviceVersion(),
                targetUri);
    }

    private void defineExceptionHandler(RouteDefinition route) {
        route.onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    scmExchangeMdc.put(exchange);
                    observationTraceSupport.traceException(exchange, exception);
                    Map<String, String> fields = scmExchangeMdc.fields(exchange);
                    log.warn("event={} layer=gateway gatewayName={} targetKind={} protocol={} channelCode={} serviceCode={} serviceVersion={} routeId={} exchangeId={} correlationId={} durationMs={} outcome=failed failureType={} failureMessage={}",
                            RouteLogEvents.GATEWAY_REQUEST_FAILED,
                            RouteLogSupport.gatewayName(exchange),
                            RouteLogSupport.targetKind(exchange),
                            RouteLogSupport.protocol(exchange),
                            failureChannelCode(exchange),
                            failureServiceCode(exchange),
                            serviceVersion(exchange),
                            exchange.getFromRouteId(),
                            exchange.getExchangeId(),
                            fields.get("correlationId"),
                            RouteLogSupport.durationMs(exchange, RouteLogSupport.GATEWAY_START_NANOS),
                            RouteLogSupport.failureType(exception),
                            RouteLogSupport.failureMessage(exception),
                            exception);
                })
                .to(Routes.GLOBAL_ERROR_HANDLER);
    }

    private RequestContractDecoder resolveRequestDecoder(ClientContract contract) {
        RequestContractDecoder decoder = requestContractDecoders.get(contract.requestDecoder());
        if (decoder == null) {
            log.error("Gateway request decoder not found decoderName={} contract={}",
                    contract.requestDecoder(), contract.name());
            throw new IllegalStateException("Request decoder not found: " + contract.requestDecoder());
        }
        return decoder;
    }

    private void applyIncomingChannel(Exchange exchange,
                                      RuntimeRoutePlan routePlan,
                                      RuntimeServicePlan servicePlan,
                                      InboundRouteDefinition inboundRoute) {
        String channelCode = incomingChannelCodeResolver.resolve(exchange)
                .orElseGet(() -> fallbackChannelCode(routePlan, servicePlan, inboundRoute));
        if (channelCode != null) {
            exchange.setProperty(Message.CHANNEL_CODE, channelCode);
        }
        ChannelServiceAccess access = fallbackAccess(routePlan, servicePlan, inboundRoute);
        if (access != null) {
            exchange.setProperty(Message.CHANNEL_SERVICE_ACCESS, access);
        }
    }

    private String channelCode(Exchange exchange, RuntimeServicePlan servicePlan) {
        return incomingChannelCodeResolver.resolve(exchange)
                .orElseGet(() -> channelCode(servicePlan.channelServiceAccess()));
    }

    private String fallbackChannelCode(RuntimeRoutePlan routePlan,
                                       RuntimeServicePlan servicePlan,
                                       InboundRouteDefinition inboundRoute) {
        ChannelServiceAccess access = fallbackAccess(routePlan, servicePlan, inboundRoute);
        return channelCode(access);
    }

    private ChannelServiceAccess fallbackAccess(RuntimeRoutePlan routePlan,
                                                RuntimeServicePlan servicePlan,
                                                InboundRouteDefinition inboundRoute) {
        if (routePlan.targetKind() != RuntimeTargetKind.CHANNEL) {
            return null;
        }
        if (inboundRoute.channelServiceDefinition() != null
                && inboundRoute.channelServiceDefinition().getChannelServiceAccess() != null) {
            return inboundRoute.channelServiceDefinition().getChannelServiceAccess();
        }
        return servicePlan.channelServiceAccess();
    }

    private String channelCode(ChannelServiceAccess access) {
        return access != null && access.getChannel() != null ? access.getChannel().getCode() : null;
    }

    private String failureChannelCode(Exchange exchange) {
        String channelCode = exchange.getProperty(Message.CHANNEL_CODE, String.class);
        if (channelCode != null) {
            return channelCode;
        }
        RuntimeServicePlan servicePlan = exchange.getProperty(Message.RUNTIME_SERVICE_PLAN, RuntimeServicePlan.class);
        return RouteLogSupport.channelCode(servicePlan);
    }

    private String failureServiceCode(Exchange exchange) {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        if (service != null) {
            return service.getCode();
        }
        RuntimeServicePlan servicePlan = exchange.getProperty(Message.RUNTIME_SERVICE_PLAN, RuntimeServicePlan.class);
        return RouteLogSupport.serviceCode(servicePlan);
    }

    private String serviceVersion(Exchange exchange) {
        return exchange.getProperty(Message.SERVICE_VERSION, String.class);
    }
}
