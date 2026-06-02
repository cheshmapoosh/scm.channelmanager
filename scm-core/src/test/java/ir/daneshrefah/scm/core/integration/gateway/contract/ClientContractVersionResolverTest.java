package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestMultipleChannelServiceDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientContractVersionResolverTest {
    private final ClientContractVersionResolver resolver = new ClientContractVersionResolver(new ObjectMapper());

    @Test
    void pathWithoutVersionDefaultsToV1() {
        assertEquals("v1", resolver.resolve(routeDefinition("""
                {
                  "method": "POST",
                  "path": "/card/inquiry"
                }
                """)));
    }

    @Test
    void v1PathResolvesToV1() {
        assertEquals("v1", resolver.resolve(routeDefinition("""
                {
                  "method": "POST",
                  "path": "/v1/card/inquiry"
                }
                """)));
    }

    @Test
    void v2PathResolvesToV2() {
        assertEquals("v2", resolver.resolve(routeDefinition("""
                {
                  "method": "POST",
                  "path": "/v2/card/inquiry"
                }
                """)));
    }

    @Test
    void explicitVersionWinsOverPath() {
        assertEquals("v3", resolver.resolve(routeDefinition("""
                {
                  "version": "v3",
                  "method": "POST",
                  "path": "/v2/card/inquiry"
                }
                """)));
    }

    @Test
    void invalidExplicitVersionFailsFast() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(routeDefinition("""
                {
                  "version": "bad-version",
                  "method": "POST",
                  "path": "/card/inquiry"
                }
                """)));
    }

    @Test
    void routeGroupContextPathCanInferVersion() {
        RestMultipleChannelServiceDefinition parent = routeGroupDefinition("""
                {
                  "contextPath": "/v2/card",
                  "multiRouteDetails": []
                }
                """, "/v2/card");
        RestChannelServiceDefinition route = restDefinition("""
                {
                  "method": "POST",
                  "path": "/inquiry"
                }
                """, "/inquiry");

        assertEquals("v2", resolver.resolve(route, parent, parent.getContextPath()));
    }

    @Test
    void routeGroupExplicitVersionWinsOverContextPath() {
        RestMultipleChannelServiceDefinition parent = routeGroupDefinition("""
                {
                  "version": "v3",
                  "contextPath": "/v2/card",
                  "multiRouteDetails": []
                }
                """, "/v2/card");
        RestChannelServiceDefinition route = restDefinition("""
                {
                  "method": "POST",
                  "path": "/inquiry"
                }
                """, "/inquiry");

        assertEquals("v3", resolver.resolve(route, parent, parent.getContextPath()));
    }

    private ChannelServiceDefinition routeDefinition(String details) {
        ChannelServiceDefinition routeDefinition = new ChannelServiceDefinition();
        routeDefinition.setId("definition-1");
        routeDefinition.setDefinition(definition(details));
        return routeDefinition;
    }

    private RestChannelServiceDefinition restDefinition(String details, String path) {
        RestChannelServiceDefinition routeDefinition = new RestChannelServiceDefinition();
        routeDefinition.setId("route-1");
        routeDefinition.setPath(path);
        routeDefinition.setDefinition(definition(details));
        return routeDefinition;
    }

    private RestMultipleChannelServiceDefinition routeGroupDefinition(String details, String contextPath) {
        RestMultipleChannelServiceDefinition routeDefinition = new RestMultipleChannelServiceDefinition();
        routeDefinition.setId("route-group-1");
        routeDefinition.setContextPath(contextPath);
        routeDefinition.setDefinition(definition(details));
        return routeDefinition;
    }

    private Definition definition(String details) {
        Definition definition = new Definition();
        definition.setDetails(details);
        return definition;
    }
}
