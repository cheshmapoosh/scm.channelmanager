package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceAccess;

import java.util.List;

public interface ChannelServiceAccessService {

    List<ChannelServiceAccess> findAllByChannel(Channel channel);
}
