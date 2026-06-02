package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
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

    private ChannelServiceDefinition routeDefinition(String details) {
        ChannelServiceDefinition routeDefinition = new ChannelServiceDefinition();
        routeDefinition.setId("definition-1");
        routeDefinition.setType(ChannelServiceDefinitionType.INBOUND);
        routeDefinition.setDefinition(definition(details));
        return routeDefinition;
    }

    private Definition definition(String details) {
        Definition definition = new Definition();
        definition.setDetails(details);
        return definition;
    }
}
