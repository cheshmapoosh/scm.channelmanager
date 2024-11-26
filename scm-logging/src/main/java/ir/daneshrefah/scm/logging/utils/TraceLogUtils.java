package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.HttpMessageInput;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.RequiredArgsConstructor;
import org.apache.camel.tracing.SpanAdapter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TraceLogUtils {

    private final ObjectMapper objectMapper;

    public void recordMessageTrace(Message message, SpanAdapter spanAdapter) {
        populateSpanAttributes(spanAdapter, message);
        try {
            spanAdapter.setTag(LogAttribute.RESPONSE.getAttributeName(), objectMapper.writeValueAsString(message.getPayload()));
            if (message.getErrors() != null && !message.getErrors().isEmpty()) {
                spanAdapter.setTag(LogAttribute.ERRORS.getAttributeName(), objectMapper.writeValueAsString(message.getErrors()));
                spanAdapter.setError(true);
            }
        } catch (JsonProcessingException ex) {
            spanAdapter.setError(true);
        }
    }

    public void recordExceptionTrace(Exception exception, SpanAdapter spanAdapter) {
        spanAdapter.setTag(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), exception.getClass().getName());
        spanAdapter.setTag(LogAttribute.EXCEPTION_MESSAGE.getAttributeName(), exception.getMessage());
    }

    private void populateSpanAttributes(SpanAdapter span, Message message) {
        Map<LogAttribute, String> logAttributes = new HashMap<>();
        logAttributes.putAll(extractLogAttributesFromMessageInput());
        logAttributes.putAll(extractLogAttributesFromMessage(message));
        for (Map.Entry<LogAttribute, String> attributeEntry : logAttributes.entrySet()) {
            span.setTag(attributeEntry.getKey().getAttributeName(), attributeEntry.getValue());
        }
    }

    private Map<LogAttribute, String> extractLogAttributesFromMessage(Message message) {
        Map<LogAttribute, String> map = new HashMap<>();
        Header header = message.getHeader();
        if (header != null) {
            map.put(LogAttribute.MESSAGE_ID, header.getMessageId());
            map.put(LogAttribute.PARENT_MESSAGE_ID, message.getHeader().getParentMessageId());
        }
        map.put(LogAttribute.SERVICE_CODE, getServiceCode(message));
        map.put(LogAttribute.PROVIDER_RESPONSE_CODE, getHttpHeader(message));
        map.put(LogAttribute.USERNAME, AuthenticationUtils.getEffectiveUsername().orElse(""));
        map.put(LogAttribute.NICKNAME, AuthenticationUtils.getEffectiveNickname().orElse(""));
        map.put(LogAttribute.DELEGATOR_USERNAME, AuthenticationUtils.getDelegatorUsername().orElse(""));
        map.put(LogAttribute.DELEGATOR_NICKNAME, AuthenticationUtils.getDelegatorNickname().orElse(""));
        map.put(LogAttribute.THREAD_NAME, Thread.currentThread().getName());
        map.put(LogAttribute.MESSAGE_STATUS, message.getStatus().toString());
        return map;
    }

    private Map<LogAttribute, String> extractLogAttributesFromMessageInput() {
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        Map<LogAttribute, String> map = new HashMap<>();
        map.put(LogAttribute.TERMINAL_CODE, messageInput.getTerminal().getCode());
        map.put(LogAttribute.CHANNEL_CODE, messageInput.getChannel().getCode());
        map.put(LogAttribute.CLIENT_ID, messageInput.getClientId());
        map.put(LogAttribute.CORRELATION_ID, messageInput.getCorrelationId());
        map.put(LogAttribute.CLIENT_CORRELATION_ID, messageInput.getClientCorrelationId());
        map.put(LogAttribute.CLIENT_FLOW_ID, messageInput.getClientFlowId());
        map.put(LogAttribute.FLOW_ID, messageInput.getFlowId());
        map.put(LogAttribute.HOST_ADDRESS, messageInput.getServerHost());
        if (messageInput instanceof HttpMessageInput httpMessageInput) {
            map.put(LogAttribute.METHOD_TYPE, httpMessageInput.getHttpMethod());
            map.put(LogAttribute.CLIENT_REMOTE_ADDRESS, httpMessageInput.getClientRemoteAddress());
        }
        return map;
    }

    private String getServiceCode(Message message) {
        return Optional.ofNullable(message)
                .map(Message::getHeader)
                .map(Header::getService)
                .map(Service::getCode)
                .orElse(null);
    }

    private String getHttpHeader(Message message) {
        return Optional.of(message)
                .map(Message::getHeader)
                .map(Header::getHttpHeader)
                .map(Header.HttpHeader::getHttpStatusCode)
                .map(String::valueOf)
                .orElse(null);

    }
}
