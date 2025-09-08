package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.apache.camel.Header;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_CHANNEL_FIND_BY_ID;
import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_CHANNEL_LIST;

@Service
public class ChannelManagementService extends AbstractJavaService {

    private final ChannelService channelService;

    public ChannelManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ChannelService channelService) {
        super(producerTemplate, objectMapper);
        this.channelService = channelService;
    }

    @JavaService(operationCode = SVC_CHANNEL_LIST)
    public PagedResponseData<Channel> listChannel(ChannelFindRequest request) {
        return channelService.findAllChannels(request);
    }

    @JavaService(operationCode = SVC_CHANNEL_FIND_BY_ID)
    public Channel findChannelById(@Header("channelId") Short channelId) {
        return channelService.findChannelById(channelId);
    }
//
//    @JavaService(serviceCode =SVC_CHANNEL_CREATE )
//    public Channel createChannel(ChannelCreateRequest request) {
//        return channelService.createChannel(request);
//    }
//
//    @JavaService(serviceCode = SVC_CHANNEL_EDIT)
//    public Channel editChannel(ChannelEditRequest request) {
//        return channelService.editChannel(request);
//    }
//
//    @JavaService(serviceCode = SVC_CHANNEL_DELETE)
//    public void deleteChannel(ChannelDeleteRequest request) {
//        channelService.deleteChannel(request);
//    }

}
