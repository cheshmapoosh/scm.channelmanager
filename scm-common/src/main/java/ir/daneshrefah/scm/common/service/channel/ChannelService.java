package ir.daneshrefah.scm.common.service.channel;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.Channel;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-24
 */
public interface ChannelService {

    List<Channel> findAllChannels();

    Optional<Channel> findChannelById(String id);

    Optional<Channel> findChannelByCode(String code);

    PagedResponseData<Channel> findPagedChannels(ChannelFindRequest request);

    Channel createChannel(ChannelCreateRequest request);

    void deleteChannel(ChannelDeleteRequest request);

    Channel editChannel(ChannelEditRequest request);
}
