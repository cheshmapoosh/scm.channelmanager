package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.logging.constant.LogAttribute;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TraceLogUtils {

    private final ObjectMapper objectMapper ;

    public void recordMessageTrace(Message message, JsonNode input, Exception exception, Span span) {
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), messageInput.getTerminal().getCode());
        span.setAttribute(LogAttribute.CHANNEL_CODE.getAttributeName(), messageInput.getChannel().getCode());
        span.setAttribute(LogAttribute.CLIENT_ID.getAttributeName(), messageInput.getClientId());
        span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), messageInput.getCorrelationId());
        span.setAttribute(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), messageInput.getClientCorrelationId());
        span.setAttribute(LogAttribute.CLIENT_FLOW_ID.getAttributeName(), messageInput.getClientFlowId());
        span.setAttribute(LogAttribute.FLOW_ID.getAttributeName(), messageInput.getFlowId());
        span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), message.getHeader().getMessageId());
        span.setAttribute(LogAttribute.PARENT_MESSAGE_ID.getAttributeName(), message.getHeader().getParentMessageId());
        span.setAttribute(LogAttribute.SERVICE_CODE.getAttributeName(), Objects.nonNull(message.getHeader().getService()) ? message.getHeader().getService().getCode() : "");
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.THREAD_NAME.getAttributeName(), Thread.currentThread().getName());
        span.setAttribute(LogAttribute.MESSAGE_STATUS.getAttributeName(), message.getStatus().toString());
        try {
            span.setAttribute(LogAttribute.RESPONSE.getAttributeName(), objectMapper.writeValueAsString(message.getPayload()));
            span.setAttribute(LogAttribute.REQUEST.getAttributeName(), objectMapper.writeValueAsString(input));
            span.setAttribute(LogAttribute.ERRORS.getAttributeName(), objectMapper.writeValueAsString(message.getErrors()));
            span.setStatus(StatusCode.OK);
        } catch (JsonProcessingException ex) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(ex);
        }
        span.recordException(exception);
    }
}
