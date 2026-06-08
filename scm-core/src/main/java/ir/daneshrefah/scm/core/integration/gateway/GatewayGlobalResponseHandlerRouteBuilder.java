package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContract;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.FaultContractEncoder;
import ir.daneshrefah.scm.core.integration.gateway.contract.ResponseContractEncoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.observability.RouteLogEvents;
import ir.daneshrefah.scm.core.integration.observability.RouteLogSupport;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayGlobalResponseHandlerRouteBuilder extends RouteBuilder {
    private final ClientContractResolver clientContractResolver;
    private final Map<String, ResponseContractEncoder> responseContractEncoders;
    private final Map<String, FaultContractEncoder> faultContractEncoders;
    private final ScmExchangeMdc scmExchangeMdc;

    @Override
    public void configure() {
        RouteDefinition route = from(Routes.GLOBAL_RESPONSE_HANDLER);
        route.onCompletion()
                .process(exchange -> scmExchangeMdc.clear())
                .end();
        route.process(exchange -> {
                    if (exchange.getProperty(Message.GATEWAY_CHANNEL_PROTOCOL) != ProtocolType.REST) {
                        // CMNEW-119 currently has a REST client-contract surface only.
                        // TODO Add SOAP/TCP contract encoders before routing those protocols here.
                        throw new IllegalStateException("Unsupported protocol type for gateway response contract encoding");
                    }

                    scmExchangeMdc.put(exchange);
                    ClientContract contract = resolveContract(exchange);
                    Object body = exchange.getMessage().getBody();
                    Object encodedBody;
                    traceResponse(exchange);
                    if (body instanceof ScmFault fault) {
                        encodedBody = encodeFault(exchange, fault, contract);
                    } else {
                        ResponseContractEncoder responseEncoder = resolveResponseEncoder(contract);
                        encodedBody = responseEncoder.encode(exchange, contract);
                        log.info("Gateway response encoded contract={} serviceVersion={} routeId={} exchangeId={}",
                                contract.name(), serviceVersion(exchange), exchange.getFromRouteId(), exchange.getExchangeId());
                    }
                    exchange.getMessage().setBody(encodedBody);
                })
                .marshal()
                .json(JsonLibrary.Jackson);
    }

    private Object encodeFault(Exchange exchange, ScmFault fault, ClientContract contract) {
        FaultContractEncoder faultEncoder = resolveFaultEncoder(contract);
        Map<String, String> fields = scmExchangeMdc.fields(exchange);
        log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} serviceCode={} serviceVersion={} contractName={} faultEncoder={} routeId={} exchangeId={} correlationId={} outcome=started",
                RouteLogEvents.GATEWAY_FAULT_ENCODING_STARTED,
                RouteLogSupport.gatewayName(exchange),
                RouteLogSupport.targetKind(exchange),
                RouteLogSupport.protocol(exchange),
                serviceCode(exchange),
                serviceVersion(exchange),
                contract.name(),
                contract.faultEncoder(),
                exchange.getFromRouteId(),
                exchange.getExchangeId(),
                fields.get("correlationId"));
        try {
            Object encodedBody = faultEncoder.encode(exchange, fault, contract);
            Integer httpStatus = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
            log.info("event={} layer=gateway gatewayName={} targetKind={} protocol={} serviceCode={} serviceVersion={} contractName={} faultEncoder={} httpStatus={} routeId={} exchangeId={} correlationId={} outcome=success",
                    RouteLogEvents.GATEWAY_FAULT_ENCODED,
                    RouteLogSupport.gatewayName(exchange),
                    RouteLogSupport.targetKind(exchange),
                    RouteLogSupport.protocol(exchange),
                    serviceCode(exchange),
                    serviceVersion(exchange),
                    contract.name(),
                    contract.faultEncoder(),
                    httpStatus,
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"));
            return encodedBody;
        } catch (RuntimeException e) {
            log.warn("event={} layer=gateway gatewayName={} targetKind={} protocol={} serviceCode={} serviceVersion={} contractName={} faultEncoder={} routeId={} exchangeId={} correlationId={} outcome=failed failureType={} failureMessage={}",
                    RouteLogEvents.GATEWAY_FAULT_ENCODING_FAILED,
                    RouteLogSupport.gatewayName(exchange),
                    RouteLogSupport.targetKind(exchange),
                    RouteLogSupport.protocol(exchange),
                    serviceCode(exchange),
                    serviceVersion(exchange),
                    contract.name(),
                    contract.faultEncoder(),
                    exchange.getFromRouteId(),
                    exchange.getExchangeId(),
                    fields.get("correlationId"),
                    RouteLogSupport.failureType(e),
                    RouteLogSupport.failureMessage(e),
                    e);
            throw e;
        }
    }

    private ClientContract resolveContract(Exchange exchange) {
        ClientContract contract = exchange.getProperty(Message.CLIENT_CONTRACT, ClientContract.class);
        if (contract != null) {
            return contract;
        }
        GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
        ChannelServiceDefinition routeDefinition = exchange.getProperty(
                Message.CHANNEL_SERVICE_DEFINITION,
                ChannelServiceDefinition.class);
        contract = clientContractResolver.resolve(gatewayChannel, routeDefinition, serviceVersion(exchange));
        exchange.setProperty(Message.CLIENT_CONTRACT, contract);
        return contract;
    }

    private String serviceVersion(Exchange exchange) {
        return exchange.getProperty(Message.SERVICE_VERSION, String.class);
    }

    private ResponseContractEncoder resolveResponseEncoder(ClientContract contract) {
        ResponseContractEncoder encoder = responseContractEncoders.get(contract.responseEncoder());
        if (encoder == null) {
            throw new IllegalStateException("Response encoder not found: " + contract.responseEncoder());
        }
        return encoder;
    }

    private FaultContractEncoder resolveFaultEncoder(ClientContract contract) {
        FaultContractEncoder encoder = faultContractEncoders.get(contract.faultEncoder());
        if (encoder == null) {
            throw new IllegalStateException("Fault encoder not found: " + contract.faultEncoder());
        }
        return encoder;
    }

    private String serviceCode(Exchange exchange) {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        return service != null ? service.getCode() : null;
    }

    private void traceResponse(Exchange exchange) {
        TraceUtils traceUtils = TraceUtils.getInstance();
        if (traceUtils != null) {
            Service service = exchange.getProperty(Message.SERVICE, Service.class);
            traceUtils.traceScmResponse(exchange, service);
        }
    }
}
