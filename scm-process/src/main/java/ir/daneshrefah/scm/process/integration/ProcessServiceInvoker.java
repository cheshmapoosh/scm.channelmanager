package ir.daneshrefah.scm.process.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.ProcessMessageInput;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.utils.constant.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

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

    private final ChannelService channelService;
    private final MessageGenerator messageGenerator;
    private final ServiceProducerTemplate producerTemplate;

    public JsonNode callService(String serviceCode, JsonNode payload, String delegateUsername, String correlationId) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(SCM_PARAMETER_TERMINAL, null);
        headers.put(SCM_PARAMETER_ACCESS_PARAMETER, null);
        headers.put(SCM_PARAMETER_AUTHORIZATION, null);
        headers.put(SCM_PARAMETER_CLAIM_CODE, null);
        headers.put(SCM_PARAMETER_USERNAME, delegateUsername);
        headers.put(SCM_PARAMETER_CLIENT_ID, null);
        headers.put(SCM_PARAMETER_CLIENT_CORRELATION_ID, correlationId);
        headers.put(SCM_PARAMETER_CLIENT_TIMESTAMP, null);
        ProcessMessageInput input = ProcessMessageInput.builder()
                .processDefinitionKey(null)
                .processInstanceId(null)
                .taskName(null)
                .taskId(null)
                .headers(headers)
                .serviceCode(serviceCode)
                .terminalCode(null)
                .channelCode(null)
                .body(payload)
                .contentType(HTTP_HEADER_CONTENT_TYPE_JSON)
                .authorization(null)
                .serverHost(null)
                .isForCheck(false)
                .build();
        Channel channel = channelService.findChannelByCode(input.getChannelCode())
                .orElseThrow(() -> new InvalidInputException("channelCode"));
        Message message = messageGenerator.buildMessageInternal(input, channel);
        producerTemplate.callService(null, message);
        return null;
    }

    public void callServiceAsync(String serviceCode, JsonNode payload) {

    }

}
