package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessCreateRequest;
import ir.daneshrefah.scm.common.dto.channel.ChannelAccessUpdateRequest;
import ir.daneshrefah.scm.common.service.channel.ChannelServiceAccessService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.apache.camel.Header;
import org.springframework.stereotype.Component;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Component
public class ChannelServiceAccessManagement extends AbstractJavaService {

    private final ChannelServiceAccessService channelServiceAccessService;

    public ChannelServiceAccessManagement(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ChannelServiceAccessService channelServiceAccessService) {
        super(producerTemplate, objectMapper);
        this.channelServiceAccessService = channelServiceAccessService;
    }

    @JavaService(operationCode = SVC_CHANNEL_SERVICE_ACCESS_FIND_BY_SERVICE_ID)
    public List<ChannelServiceAccess> findByServiceId(@Header("serviceId") Long serviceId) {
        return channelServiceAccessService.findAllByServiceId(serviceId);
    }

    @JavaService(operationCode = SVC_CHANNEL_SERVICE_ACCESS_CREATE)
    public ChannelServiceAccess create(ChannelAccessCreateRequest request) {
        return channelServiceAccessService.create(request);
    }

    @JavaService(operationCode = SVC_CHANNEL_SERVICE_ACCESS_UPDATE)
    public ChannelServiceAccess update(ChannelAccessUpdateRequest request) {
        return channelServiceAccessService.update(request);
    }
}
