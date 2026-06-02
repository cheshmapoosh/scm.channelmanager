package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultClientContractResolverTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientContractVersionResolver versionResolver = new ClientContractVersionResolver(objectMapper);
    private final DefaultClientContractResolver resolver = new DefaultClientContractResolver(objectMapper, versionResolver);

    @Test
    void resolvesContractFromRouteDefinitionDetails() {
        ClientContract contract = resolver.resolve(restGateway(), routeDefinition("""
                {
                  "method": "POST",
                  "path": "/card/inquiry",
                  "contract": {
                    "name": "card-inquiry-v1",
                    "requestDecoder": "cardInquiryV1RequestDecoder",
                    "responseEncoder": "cardInquiryV1ResponseEncoder",
                    "faultEncoder": "cardInquiryV1FaultEncoder"
                  }
                }
                """));

        assertEquals("card-inquiry-v1", contract.name());
        assertEquals("cardInquiryV1RequestDecoder", contract.requestDecoder());
        assertEquals("cardInquiryV1ResponseEncoder", contract.responseEncoder());
        assertEquals("cardInquiryV1FaultEncoder", contract.faultEncoder());
        assertEquals("v1", contract.version());
    }

    @Test
    void resolvesContractWithExplicitVersion() {
        ClientContract contract = resolver.resolve(restGateway(), routeDefinition("""
                {
                  "version": "v2",
                  "method": "POST",
                  "path": "/v2/cards/inquiry",
                  "contract": {
                    "name": "card-inquiry-v2",
                    "requestDecoder": "cardInquiryV2RequestDecoder",
                    "responseEncoder": "cardInquiryV2ResponseEncoder",
                    "faultEncoder": "cardInquiryV2FaultEncoder"
                  }
                }
                """));

        assertEquals("card-inquiry-v2", contract.name());
        assertEquals("v2", contract.version());
    }

    @Test
    void fallsBackToDefaultRestContract() {
        ClientContract contract = resolver.resolve(restGateway(), null);

        assertEquals("rest-default", contract.name());
        assertEquals("jsonScmRequestDecoder", contract.requestDecoder());
        assertEquals("jsonScmResponseEncoder", contract.responseEncoder());
        assertEquals("restProblemDetailFaultEncoder", contract.faultEncoder());
        assertEquals("v1", contract.version());
    }

    @Test
    void fallbackContractKeepsSuppliedServiceVersion() {
        ClientContract contract = resolver.resolve(restGateway(), null, "v2");

        assertEquals("rest-default", contract.name());
        assertEquals("v2", contract.version());
    }

    @Test
    void ignoresContractUnderSvcDomainMember() {
        ChannelServiceDefinition membershipDefinition = routeDefinition("""
                {
                  "contract": {
                    "name": "ignored",
                    "requestDecoder": "ignoredDecoder",
                    "responseEncoder": "ignoredEncoder",
                    "faultEncoder": "ignoredFaultEncoder"
                  }
                }
                """);
        membershipDefinition.setType(ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER);

        ClientContract contract = resolver.resolve(restGateway(), membershipDefinition);

        assertEquals("rest-default", contract.name());
    }

    @Test
    void ignoresContractUnderApiDoc() {
        ChannelServiceDefinition apiDocDefinition = routeDefinition("""
                {
                  "contract": {
                    "name": "ignored",
                    "requestDecoder": "ignoredDecoder",
                    "responseEncoder": "ignoredEncoder",
                    "faultEncoder": "ignoredFaultEncoder"
                  }
                }
                """);
        apiDocDefinition.setType(ChannelServiceDefinitionType.API_DOC);

        ClientContract contract = resolver.resolve(restGateway(), apiDocDefinition);

        assertEquals("rest-default", contract.name());
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
        channelServiceDefinition.setType(ChannelServiceDefinitionType.INBOUND);
        channelServiceDefinition.setDefinition(definition);
        return channelServiceDefinition;
    }
}
