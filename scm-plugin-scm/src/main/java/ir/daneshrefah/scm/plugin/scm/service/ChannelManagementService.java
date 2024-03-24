package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.service.ChannelInfoRequest;
import ir.daneshrefah.scm.common.service.ChannelService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
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

    public PagedResponseData<Channel> listChannel(ChannelInfoRequest request) {
        return channelService.findPagedChannels(request);
    }

}
