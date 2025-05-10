package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;

public interface ChannelServiceDefinitionService {
    RestChannelServiceDefinition findRestDefinition(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel);
}
