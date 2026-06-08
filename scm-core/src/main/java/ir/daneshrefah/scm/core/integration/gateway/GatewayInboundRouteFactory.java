package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.protocol.ProtocolType;

import java.util.List;

public interface GatewayInboundRouteFactory {
    ProtocolType protocol();

    List<InboundRouteDefinition> createRoutes(GatewayInboundRouteContext context);
}
