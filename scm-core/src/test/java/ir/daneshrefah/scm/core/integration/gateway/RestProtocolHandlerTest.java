package ir.daneshrefah.scm.core.integration.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestMultipleChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractVersionResolver;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestProtocolHandlerTest {
    private final RestProtocolHandler handler = new RestProtocolHandler(
            new ClientContractVersionResolver(new ObjectMapper()));

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
    void createsRoutesFromInboundRoute() throws Exception {
        RestChannelServiceDefinition routeDefinition = restRoute("/v2/card/inquiry");

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("domain.card"),
                servicePlan("domain.card", List.of(routeDefinition)));

        assertEquals(1, routes.size());
        assertEquals("card-inquiry-v2-route", routes.getFirst().route().getRouteId());
        assertEquals("v2", routes.getFirst().serviceVersion());
    }

    @Test
    void createsRoutesFromInboundRouteGroup() throws Exception {
        RestMultipleChannelServiceDefinition groupDefinition = new RestMultipleChannelServiceDefinition();
        groupDefinition.setType(ChannelServiceDefinitionType.INBOUND_ROUTE_GROUP);
        groupDefinition.setContextPath("/v2/card");

        RestMultipleChannelServiceDefinition.MultiRouteDetail routeDetail =
                new RestMultipleChannelServiceDefinition.MultiRouteDetail();
        routeDetail.setOperationCode("CARD_INQUIRY");
        routeDetail.setDefinition(restRoute("/inquiry"));
        groupDefinition.setMultiRouteDetails(List.of(routeDetail));

        List<InboundRouteDefinition> routes = routeDefinitions(
                gateway("domain.card"),
                servicePlan("domain.card", List.of(groupDefinition)));

        assertEquals(1, routes.size());
        assertTrue(routes.getFirst().route().getRouteId().startsWith("card-inquiry-v2-route-"));
        assertEquals("v2", routes.getFirst().serviceVersion());
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

    private RestChannelServiceDefinition restRoute(String path) {
        RestChannelServiceDefinition routeDefinition = new RestChannelServiceDefinition();
        routeDefinition.setType(ChannelServiceDefinitionType.INBOUND_ROUTE);
        routeDefinition.setMethod(HttpMethod.POST);
        routeDefinition.setPath(path);
        return routeDefinition;
    }
}
