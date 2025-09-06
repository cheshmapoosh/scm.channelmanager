package ir.daneshrefah.scm.common.service.channel;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;

import java.util.List;

public interface ChannelServiceAccessService {
    List<ChannelServiceAccess> findAllByChannel(Channel channel);
    List<ChannelServiceAccess> findAllByTerminalType(TerminalType terminalType);
    List<ChannelServiceAccess> findAll();
    List<ChannelServiceAccess> findAllByServiceId(Short serviceId);
    ChannelServiceAccess create(ChannelServiceAccess request);
}
