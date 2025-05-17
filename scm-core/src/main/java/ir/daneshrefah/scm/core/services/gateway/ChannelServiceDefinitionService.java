package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

import java.util.List;

public interface ChannelServiceDefinitionService {
    List<ChannelServiceDefinition> findDefinitions(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel);
}
