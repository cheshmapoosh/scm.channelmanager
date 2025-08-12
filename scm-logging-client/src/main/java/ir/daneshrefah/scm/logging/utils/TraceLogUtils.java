//package ir.daneshrefah.scm.logging.utils;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.daneshrefah.scm.common.constant.log.LogAttribute;
//import ir.daneshrefah.scm.common.model.message.*;
//import ir.daneshrefah.scm.common.model.service.ScmService;
//import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
//import ir.daneshrefah.scm.utils.MessageInputContext;
//import lombok.RequiredArgsConstructor;
//import org.apache.camel.Exchange;
//import org.apache.camel.tracing.SpanAdapter;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Component //TODO Delete this class
//@RequiredArgsConstructor
//public class TraceLogUtils {
//
//    private final ObjectMapper objectMapper;
//    private static final String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";
//    private static final String HEADER_MESSAGE_OUTPUT = "ScmMessageOutput";
//
//    public void recordMessageTrace(Exchange exchange, SpanAdapter spanAdapter) {
//        Message message = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
//        recordMessageTrace(message, spanAdapter);
//        recordMessageOutPutTrace(exchange, spanAdapter);
//    }
//
//    private void recordMessageOutPutTrace(Exchange exchange, SpanAdapter spanAdapter) {
//        Object object = exchange.getProperty(HEADER_MESSAGE_OUTPUT);
//        if (object instanceof MessageOutput messageOutput) {
//            try {
//                spanAdapter.setTag(LogAttribute.REQUEST.getAttributeName(), objectMapper.writeValueAsString(messageOutput.getBody()));
//            } catch (JsonProcessingException ex) {
//                spanAdapter.setError(true);
//            }
//        }
//    }
//
//    public void recordMessageTrace(Message message, SpanAdapter spanAdapter) {
//        populateSpanAttributes(spanAdapter, message);
//        try {
//            spanAdapter.setTag(LogAttribute.RESPONSE.getAttributeName(), objectMapper.writeValueAsString(message.getPayload()));
//            if (message.getErrors() != null && !message.getErrors().isEmpty()) {
//                spanAdapter.setTag(LogAttribute.ERRORS.getAttributeName(), objectMapper.writeValueAsString(message.getErrors()));
//                spanAdapter.setError(true);
//            }
//        } catch (JsonProcessingException ex) {
//            spanAdapter.setError(true);
//        }
//    }
//
//    public void recordExceptionTrace(Exception ex, SpanAdapter spanAdapter) {
//        StackTraceElement[] stackTraceElements = Arrays.stream(ex.getStackTrace()).limit(3).toArray(StackTraceElement[]::new);
//        ex.setStackTrace(stackTraceElements);
//        String stackTraceString = ExceptionUtils.getStackTrace(ex);
//        spanAdapter.setTag(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
//        spanAdapter.setTag(LogAttribute.ERROR_DETAILS.getAttributeName(), stackTraceString);
//    }
//
//    private void populateSpanAttributes(SpanAdapter span, Message message) {
//        Map<LogAttribute, String> logAttributes = new HashMap<>();
//        logAttributes.putAll(extractLogAttributesFromMessageInput());
//        logAttributes.putAll(extractLogAttributesFromMessage(message));
//        for (Map.Entry<LogAttribute, String> attributeEntry : logAttributes.entrySet()) {
//            span.setTag(attributeEntry.getKey().getAttributeName(), attributeEntry.getValue());
//        }
//    }
//
//    private Map<LogAttribute, String> extractLogAttributesFromMessage(Message message) {
//        Map<LogAttribute, String> map = new HashMap<>();
//        Header header = message.getHeader();
//        if (header != null) {
//            map.put(LogAttribute.MESSAGE_ID, header.getMessageId());
//            map.put(LogAttribute.PARENT_MESSAGE_ID, message.getHeader().getParentMessageId());
//        }
//        map.put(LogAttribute.SERVICE_CODE, getServiceCode(message));
//        map.put(LogAttribute.PROVIDER_RESPONSE_CODE, getHttpHeader(message));
//        map.put(LogAttribute.USERNAME, AuthenticationUtils.getEffectiveUsername().orElse(""));
//        map.put(LogAttribute.NICKNAME, AuthenticationUtils.getEffectiveNickname().orElse(""));
//        map.put(LogAttribute.DELEGATOR_USERNAME, AuthenticationUtils.getDelegatorUsername().orElse(""));
//        map.put(LogAttribute.DELEGATOR_NICKNAME, AuthenticationUtils.getDelegatorNickname().orElse(""));
//        map.put(LogAttribute.THREAD_NAME, Thread.currentThread().getName());
//        map.put(LogAttribute.MESSAGE_STATUS, message.getStatus().toString());
//        return map;
//    }
//
//    private Map<LogAttribute, String> extractLogAttributesFromMessageInput() {
//        MessageInput messageInput = MessageInputContext.getCurrentContext();
//        Map<LogAttribute, String> map = new HashMap<>();
//        map.put(LogAttribute.TERMINAL_CODE, messageInput.getTerminal().getCode());
//        map.put(LogAttribute.CHANNEL_CODE, messageInput.getChannel().getCode());
//        map.put(LogAttribute.CLIENT_ID, messageInput.getClientId());
//        map.put(LogAttribute.CORRELATION_ID, messageInput.getCorrelationId());
//        map.put(LogAttribute.CLIENT_CORRELATION_ID, messageInput.getClientCorrelationId());
//        map.put(LogAttribute.CLIENT_FLOW_ID, messageInput.getClientFlowId());
//        map.put(LogAttribute.FLOW_ID, messageInput.getFlowId());
//        map.put(LogAttribute.HOST_ADDRESS, messageInput.getServerHost());
//        if (messageInput instanceof HttpMessageInput httpMessageInput) {
//            map.put(LogAttribute.METHOD_TYPE, httpMessageInput.getHttpMethod());
//            map.put(LogAttribute.CLIENT_REMOTE_ADDRESS, httpMessageInput.getClientRemoteAddress());
//        }
//        return map;
//    }
//
//    private String getServiceCode(Message message) {
//        return Optional.ofNullable(message)
//                .map(Message::getHeader)
//                .map(Header::getService)
//                .map(ScmService::getCode)
//                .orElse(null);
//    }
//
//    private String getHttpHeader(Message message) {
//        return Optional.of(message)
//                .map(Message::getHeader)
//                .map(Header::getHttpHeader)
//                .map(Header.HttpHeader::getHttpStatusCode)
//                .map(String::valueOf)
//                .orElse(null);
//
//    }
//}
