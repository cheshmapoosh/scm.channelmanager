package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowExecutionIdentityResolver;
import org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component("jsonScmRequestDecoder")
@RequiredArgsConstructor
public class JsonScmRequestDecoder implements RequestContractDecoder {
    private final ObjectMapper objectMapper;

    @Override
    public void decode(Exchange exchange, ClientContract contract) {
        Object body = exchange.getMessage().getBody(JsonNode.class);
        JsonNode payload = toJsonNode(body);
        exchange.setProperty(Message.ORIGINAL_BODY, body);
        exchange.setProperty(Message.ORIGINAL_HEADERS, new LinkedHashMap<>(exchange.getMessage().getHeaders()));
        exchange.setProperty(Message.INTERNAL_MESSAGE, Message.builder()
                .header(Header.builder().build())
                .status(MessageStatus.SC_PROCESSING)
                .payload(payload)
                .build());
        normalizeExecutionId(exchange, payload);
    }

    protected JsonNode toJsonNode(Object body) {
        if (body instanceof JsonNode jsonNode) {
            return jsonNode;
        }
        if (body instanceof String text) {
            try {
                return objectMapper.readTree(text);
            } catch (Exception ignored) {
                return objectMapper.valueToTree(text);
            }
        }
        return objectMapper.valueToTree(body);
    }

    private void normalizeExecutionId(Exchange exchange, JsonNode payload) {
        String payloadValue = null;
        if (payload != null && payload.isObject()
                && payload.has("executionId")
                && !payload.get("executionId").isNull()) {
            if (!payload.get("executionId").isTextual()) {
                throw new IllegalArgumentException(
                        "executionId must be a string");
            }
            payloadValue = StringUtils.trimToNull(
                    payload.get("executionId").textValue());
            if (payloadValue == null) {
                throw new IllegalArgumentException(
                        "executionId must not be blank");
            }
        }
        String headerValue = StringUtils.trimToNull(
                exchange.getMessage().getHeader(
                        TaskWorkflowExecutionIdentityResolver.EXECUTION_ID_HEADER,
                        String.class
                )
        );
        if (payloadValue != null && headerValue != null
                && !payloadValue.equals(headerValue)) {
            throw new IllegalArgumentException(
                    "Conflicting executionId values in payload and "
                            + TaskWorkflowExecutionIdentityResolver.EXECUTION_ID_HEADER);
        }
        String resolved = payloadValue == null ? headerValue : payloadValue;
        if (resolved != null) {
            exchange.setProperty(Message.EXECUTION_ID, resolved);
        }
    }
}
