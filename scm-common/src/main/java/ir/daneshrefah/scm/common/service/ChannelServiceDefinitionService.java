package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionCreateRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionResponse;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;

import java.util.List;

public interface ChannelServiceDefinitionService {
    List<ChannelServiceDefinition> findDefinitions(ChannelServiceAccess channelServiceAccess, GatewayChannel gatewayChannel);

    List<ChannelServiceDefinitionResponse> findDefinitionsByChannelServiceAccess(ChannelServiceDefinitionRequest request);

    ChannelServiceDefinitionResponse create(ChannelServiceDefinitionCreateRequest request);
}
