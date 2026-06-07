package ir.daneshrefah.scm.core.integration.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.*;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractVersionResolver;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestProtocolHandlerTest {
    private final RestProtocolHandler handler = new RestProtocolHandler(
            new ClientContractVersionResolver(new ObjectMapper()));

    @Test
    void createsRouteFromInbound() throws Exception {
        InboundChannelServiceDefinition routeDefinition = restRoute("route-1", "/v2/card/inquiry");

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("domain.card"),
                servicePlan("domain.card", List.of(routeDefinition)));

        assertEquals(1, routes.size());
        assertEquals("gw.dm.domain-card.card-inquiry.v2", routes.getFirst().route().getRouteId());
        assertEquals("v2", routes.getFirst().serviceVersion());
    }

    @Test
    void doesNotCreateRoutesFromApiDoc() throws Exception {
        ChannelServiceDefinition apiDocDefinition = new ChannelServiceDefinition();
        apiDocDefinition.setType(ChannelServiceDefinitionType.API_DOC);

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("domain.card"),
                servicePlan("domain.card", List.of(apiDocDefinition)));

        assertTrue(routes.isEmpty());
    }

    @Test
    void doesNotCreateRoutesFromSvcDomainMember() throws Exception {
        ChannelServiceDefinition memberDefinition = new ChannelServiceDefinition();
        memberDefinition.setType(ChannelServiceDefinitionType.SVC_DOMAIN_MEMBER);

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("domain.card"),
                servicePlan("domain.card", List.of(memberDefinition)));

        assertTrue(routes.isEmpty());
    }

    @Test
    void doesNotCreateDefaultRouteWhenInboundIsMissing() throws Exception {
        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("channel.mb"),
                servicePlan("channel.mb", List.of()));

        assertTrue(routes.isEmpty());
    }

    @Test
    void duplicateServiceVersionInboundRoutesReceiveUniqueRouteIds() throws Exception {
        InboundChannelServiceDefinition inquiry = restRoute("route-1", "/v2/card/inquiry");
        InboundChannelServiceDefinition status = restRoute("route-2", "/v2/card/status");

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("domain.card"),
                servicePlan("domain.card", List.of(inquiry, status)));

        assertEquals(2, routes.size());
        assertEquals("gw.dm.domain-card.card-inquiry.v2", routes.get(0).route().getRouteId());
        assertTrue(routes.get(1).route().getRouteId().startsWith("gw.dm.domain-card.card-inquiry.v2."));
        assertNotEquals(routes.get(0).route().getRouteId(), routes.get(1).route().getRouteId());
    }

    @Test
    void channelGatewayRouteIdUsesChannelShortName() throws Exception {
        InboundChannelServiceDefinition routeDefinition = restRoute("route-1", "/v1/card/inquiry");

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("channel.mb"),
                servicePlan("channel.mb", List.of(routeDefinition)));

        assertEquals("gw.ch.channel-mb.card-inquiry.v1", routes.getFirst().route().getRouteId());
    }

    private List<InboundRouteDefinition> routeDefinitions(GatewayChannel gatewayChannel,
                                                         RuntimeServicePlan servicePlan) throws Exception {
        AtomicReference<List<InboundRouteDefinition>> routes = new AtomicReference<>();
        try (DefaultCamelContext context = new DefaultCamelContext()) {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    routes.set(handler.config(gatewayChannel, this).routeDefinition(servicePlan));
                }
            });
        }
        return routes.get();
    }

    private RuntimeServicePlan servicePlan(String gatewayName, List<ChannelServiceDefinition> definitions) {
        return new RuntimeServicePlan(gateway(gatewayName), null, service(), definitions);
    }

    private GatewayChannel gateway(String name) {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setName(name);
        gatewayChannel.setPath("/");
        return gatewayChannel;
    }

    private Service service() {
        Service service = new Service();
        service.setCode("card-inquiry");
        return service;
    }

    private InboundChannelServiceDefinition restRoute(String id, String path) {
        InboundChannelServiceDefinition routeDefinition = new InboundChannelServiceDefinition();
        routeDefinition.setId(id);
        routeDefinition.setType(ChannelServiceDefinitionType.INBOUND);
        routeDefinition.setMethod(HttpMethod.POST);
        routeDefinition.setPath(path);
        return routeDefinition;
    }
}
