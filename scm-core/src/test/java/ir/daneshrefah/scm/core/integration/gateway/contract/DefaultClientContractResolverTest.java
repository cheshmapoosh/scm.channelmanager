package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultClientContractResolverTest {
    private final DefaultClientContractResolver resolver = new DefaultClientContractResolver(new ObjectMapper());

    @Test
    void resolvesContractFromRouteDefinitionDetails() {
        ClientContract contract = resolver.resolve(restGateway(), routeDefinition("""
                {
                  "method": "POST",
                  "path": "/legacy/cards/inquiry",
                  "contract": {
                    "name": "legacy-mb-card-v1",
                    "requestDecoder": "legacyMbCardRequestDecoder",
                    "responseEncoder": "legacyMbCardResponseEncoder",
                    "faultEncoder": "legacyMbCardFaultEncoder"
                  }
                }
                """));

        assertEquals("legacy-mb-card-v1", contract.name());
        assertEquals("legacyMbCardRequestDecoder", contract.requestDecoder());
        assertEquals("legacyMbCardResponseEncoder", contract.responseEncoder());
        assertEquals("legacyMbCardFaultEncoder", contract.faultEncoder());
    }

    @Test
    void fallsBackToModernRestContract() {
        ClientContract contract = resolver.resolve(restGateway(), null);

        assertEquals("modern-rest-v1", contract.name());
        assertEquals("jsonScmRequestDecoder", contract.requestDecoder());
        assertEquals("jsonScmResponseEncoder", contract.responseEncoder());
        assertEquals("restProblemDetailFaultEncoder", contract.faultEncoder());
    }

    private GatewayChannel restGateway() {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setProtocolType(ProtocolType.REST);
        return gatewayChannel;
    }

    private ChannelServiceDefinition routeDefinition(String details) {
        Definition definition = new Definition();
        definition.setDetails(details);
        ChannelServiceDefinition channelServiceDefinition = new ChannelServiceDefinition();
        channelServiceDefinition.setId("definition-1");
        channelServiceDefinition.setDefinition(definition);
        return channelServiceDefinition;
    }
}
