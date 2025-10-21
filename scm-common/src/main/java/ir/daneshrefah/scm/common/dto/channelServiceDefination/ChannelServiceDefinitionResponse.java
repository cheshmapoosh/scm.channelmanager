package ir.daneshrefah.scm.common.dto.channelServiceDefination;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelServiceDefinitionResponse extends PagedRequestData {
    private String id;
    private ChannelServiceAccess channelServiceAccess;
    private GatewayChannel gatewayChannel;
    private ChannelServiceDefinitionType type;
    private Definition definition;
}
