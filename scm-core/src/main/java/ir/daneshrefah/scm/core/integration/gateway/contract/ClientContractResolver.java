package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

public interface ClientContractResolver {
    ClientContract resolve(GatewayChannel gatewayChannel,
                           ChannelServiceDefinition routeDefinition,
                           String serviceVersion);

    default ClientContract resolve(GatewayChannel gatewayChannel, ChannelServiceDefinition routeDefinition) {
        return resolve(gatewayChannel, routeDefinition, null);
    }
}
