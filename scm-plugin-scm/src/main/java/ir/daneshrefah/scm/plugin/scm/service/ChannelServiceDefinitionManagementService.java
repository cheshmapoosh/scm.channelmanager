package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionCreateRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionRequest;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionResponse;
import ir.daneshrefah.scm.common.service.ChannelServiceDefinitionService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceDefinitionManagementService extends AbstractJavaService {

    private final ChannelServiceDefinitionService channelServiceDefinitionService;

    public ChannelServiceDefinitionManagementService(ServiceProducerTemplate producerTemplate,
                                                     ObjectMapper objectMapper,
                                                     ChannelServiceDefinitionService channelServiceDefinitionService) {
        super(producerTemplate, objectMapper);
        this.channelServiceDefinitionService = channelServiceDefinitionService;
    }

    @JavaService(operationCode = OperationCode.SVC_CHANNEL_SERVICE_DEFINITION_LIST)
    public List<ChannelServiceDefinitionResponse> findByServiceIdAndChannelId(ChannelServiceDefinitionRequest request) {
        return channelServiceDefinitionService.findDefinitionsByChannelServiceAccess(request);
    }

    @JavaService(operationCode = OperationCode.SVC_CHANNEL_SERVICE_DEFINITION_CREATE)
    public ChannelServiceDefinitionResponse create(ChannelServiceDefinitionCreateRequest request) {
        return channelServiceDefinitionService.create(request);
    }
}
