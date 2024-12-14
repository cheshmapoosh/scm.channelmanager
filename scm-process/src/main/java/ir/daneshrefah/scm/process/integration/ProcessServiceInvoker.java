package ir.daneshrefah.scm.process.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.ProcessMessageInput;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.process.config.ProcessProperties;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.base64.Base64Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.utils.constant.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;
import static ir.daneshrefah.scm.utils.string.StringUtils.COLON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-06
 */
@RequiredArgsConstructor
@Component
public class ProcessServiceInvoker {
    private static final String BPMS_CHANNEL_CODE = "BPMS";
    private static ProcessServiceInvoker INSTANCE;
    private final ProcessProperties properties;
    private final ChannelService channelService;
    private final MessageGenerator messageGenerator;
    private final ServiceProducerTemplate producerTemplate;
    private final ServiceService serviceService;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public JsonNode callService(String serviceCode, JsonNode payload, ProcessMessageInput processMessage) throws Exception {
        String authorizationHeader = "Basic " + Base64Utils.encodeWithBase64(properties.getClientId() + COLON + properties.getClientSecret());
        Channel channel = channelService.findChannelByCode(BPMS_CHANNEL_CODE)
                .orElseThrow(() -> new InvalidInputException("channelCode"));
        MessageInput.IgnoreCaseHeader headers = new MessageInput.IgnoreCaseHeader();
        headers.put(SCM_PARAMETER_TERMINAL, null);
        headers.put(SCM_PARAMETER_ACCESS_PARAMETER, "123");//TODO processMessage.getAccessParameter() is null
//        headers.put(SCM_PARAMETER_AUTHORIZATION, null);
//        headers.put(SCM_PARAMETER_CLAIM_CODE, null);
        headers.put(SCM_PARAMETER_USERNAME, "");
//        headers.put(SCM_PARAMETER_CLIENT_ID, null);
//        headers.put(SCM_PARAMETER_CLIENT_CORRELATION_ID, correlationId);
//        headers.put(SCM_PARAMETER_CLIENT_TIMESTAMP, null);
        ProcessMessageInput input = ProcessMessageInput.builder()
                .processDefinitionKey(processMessage.getProcessDefinitionKey())
                .processInstanceId(processMessage.getProcessInstanceId())
                .taskName(processMessage.getTaskName())
                .taskId(processMessage.getTaskId())
                .headers(headers)
                .serviceCode(serviceCode)
                .terminalCode(channel.getTerminal().getCode())
                .terminal(channel.getTerminal())
                .channel(channel)
                .body(payload)
                .contentType(HTTP_HEADER_CONTENT_TYPE_JSON)
                .serverHost(null)
                .isForCheck(false)
                .build();
        MessageInputContext.init(input);
        Message message = messageGenerator.buildMessageFromInput();
        Service service = serviceService.findServiceByCode(serviceCode);
        Message msgResponse = producerTemplate.callService(service, message);
        return msgResponse.getPayload();
    }

    public void callServiceAsync(String serviceCode, JsonNode payload) {
    }

    public static ProcessServiceInvoker getInstance() {
        return INSTANCE;
    }
}
