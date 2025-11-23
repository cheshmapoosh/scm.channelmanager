package ir.daneshrefah.scm.common.service.channel;

import ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.gateway.Channel;

import java.util.Optional;

public interface ChannelService {

    PagedResponseData<Channel> findAllChannels(ChannelFindRequest request);
//    Optional<Channel> findChannelById(String id);
//
    Optional<Channel> findChannelByCode(String code);

    Channel findChannelById(Short id);
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
