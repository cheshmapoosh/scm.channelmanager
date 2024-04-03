package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.channel.*;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-24
 */
@Service
public class ChannelManagementService extends AbstractJavaService {

    private final ChannelService channelService;

    public ChannelManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ChannelService channelService) {
        super(producerTemplate, objectMapper);
        this.channelService = channelService;
    }

    public PagedResponseData<Channel> listChannel(ChannelFindRequest request) {
        return channelService.findPagedChannels(request);
    }

    public Channel findChannelById(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            throw new MissingRequiredInputException("channelId");
        }
        Optional<Channel> channelById = channelService.findChannelById(channelId);
        if (channelById.isEmpty()) {
            throw new NoMatchRecordFoundException("channel");
        }
        return channelById.get();
    }

    public Channel createChannel(ChannelCreateRequest request) {
        return null;
    }

    public Channel editChannel(ChannelEditRequest request) {
        return null;
    }

    public Channel deleteChannel(ChannelDeleteRequest request) {
        return null;
    }

}
