package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.channel.ChannelCreateRequest;
import ir.daneshrefah.scm.common.dto.channel.ChannelDeleteRequest;
import ir.daneshrefah.scm.common.dto.channel.ChannelEditRequest;
import ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.service.channel.*;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

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

    @JavaService
    public PagedResponseData<Channel> listChannel(ChannelFindRequest request) {
        return channelService.findPagedChannels(request);
    }

    @JavaService
    public Channel findChannelById(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            throw new MissingRequiredInputException("channelId");
        }
        return channelService
                .findChannelById(channelId)
                .orElseThrow(()->new NoMatchRecordFoundException("channel"));
    }

    @JavaService
    public Channel createChannel(ChannelCreateRequest request) {
        return channelService.createChannel(request);
    }

    @JavaService
    public Channel editChannel(ChannelEditRequest request) {
        return channelService.editChannel(request);
    }

    @JavaService
    public void deleteChannel(ChannelDeleteRequest request) {
        channelService.deleteChannel(request);
    }

}
