package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessCreateRequest;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessUpdateRequest;
import ir.daneshrefah.scm.common.model.gateway.Channel;

import java.util.List;

public interface ChannelServiceAccessService {
    List<ChannelServiceAccess> findAllByChannel(Channel channel);

    List<ChannelServiceAccess> findAllByTerminalType(TerminalType terminalType);

    List<ChannelServiceAccess> findAll();

    List<ChannelServiceAccess> findAllByServiceId(Long serviceId);

    ChannelServiceAccess findByChannelAndServiceId(Short channelId, Short serviceId);

    ChannelServiceAccess findById(Long serviceId);

    ChannelServiceAccess create(ChannelAccessCreateRequest request);

    ChannelServiceAccess update(ChannelAccessUpdateRequest request);
}
