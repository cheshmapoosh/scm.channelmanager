package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

public interface ClientContractResolver {
    ClientContract resolve(GatewayChannel gatewayChannel, ChannelServiceDefinition routeDefinition);
}
