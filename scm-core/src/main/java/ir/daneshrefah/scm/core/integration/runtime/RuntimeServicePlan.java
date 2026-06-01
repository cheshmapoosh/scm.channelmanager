package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;

import java.util.List;

public record RuntimeServicePlan(
        GatewayChannel gatewayChannel,
        ChannelServiceAccess channelServiceAccess,
        Service service,
        List<ChannelServiceDefinition> routeDefinitions) {
}
