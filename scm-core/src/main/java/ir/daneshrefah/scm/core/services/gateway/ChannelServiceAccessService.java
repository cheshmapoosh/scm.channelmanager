package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;

import java.util.List;

public interface ChannelServiceAccessService {

    List<ChannelServiceAccess> findAllByChannel(Channel channel);
}
