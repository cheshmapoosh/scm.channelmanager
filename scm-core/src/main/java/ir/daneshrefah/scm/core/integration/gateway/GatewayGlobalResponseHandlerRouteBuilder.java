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
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                        FaultContractEncoder faultEncoder = resolveFaultEncoder(contract);
                        encodedBody = faultEncoder.encode(exchange, fault, contract);
                        log.info("Gateway fault encoded contract={} routeId={} exchangeId={}",
                                contract.name(), exchange.getFromRouteId(), exchange.getExchangeId());
                    } else {
                        ResponseContractEncoder responseEncoder = resolveResponseEncoder(contract);
                        encodedBody = responseEncoder.encode(exchange, contract);
                        log.info("Gateway response encoded contract={} routeId={} exchangeId={}",
                                contract.name(), exchange.getFromRouteId(), exchange.getExchangeId());
                    }
                    exchange.getMessage().setBody(encodedBody);
                })
                .marshal()
                .json(JsonLibrary.Jackson);
    }

    private ClientContract resolveContract(org.apache.camel.Exchange exchange) {
        ClientContract contract = exchange.getProperty(Message.CLIENT_CONTRACT, ClientContract.class);
        if (contract != null) {
            return contract;
        }
        GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
        ChannelServiceDefinition routeDefinition = exchange.getProperty(
                Message.CHANNEL_SERVICE_DEFINITION,
                ChannelServiceDefinition.class);
        contract = clientContractResolver.resolve(gatewayChannel, routeDefinition);
        exchange.setProperty(Message.CLIENT_CONTRACT, contract);
        return contract;
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

    private void traceResponse(org.apache.camel.Exchange exchange) {
        TraceUtils traceUtils = TraceUtils.getInstance();
        if (traceUtils != null) {
            Service service = exchange.getProperty(Message.SERVICE, Service.class);
            traceUtils.traceScmResponse(exchange, service);
        }
    }
}
