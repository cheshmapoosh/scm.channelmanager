package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.Channel;

import java.util.Optional;

public interface ChannelService {
    //    List<Channel> findAllChannels();
//
//    Optional<Channel> findChannelById(String id);
//
    Optional<Channel> findChannelByCode(String code);
//
//    PagedResponseData<Channel> findPagedChannels(ChannelFindRequest request);
//
//    Channel createChannel(ChannelCreateRequest request);
//
//    void deleteChannel(ChannelDeleteRequest request);
//
//    Channel editChannel(ChannelEditRequest request);
//}
}
