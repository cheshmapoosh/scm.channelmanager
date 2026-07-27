package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

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
        normalizeClientCorrelationId(exchange);
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
        String decodedValue = StringUtils.trimToNull(
                exchange.getProperty(Message.EXECUTION_ID, String.class)
        );
        if (payloadValue != null && decodedValue != null
                && !payloadValue.equals(decodedValue)) {
            throw new IllegalArgumentException(
                    "Conflicting executionId values in normalized input"
            );
        }
        String resolved = payloadValue == null ? decodedValue : payloadValue;
        if (resolved != null) {
            exchange.setProperty(Message.EXECUTION_ID, resolved);
        }
    }

    private void normalizeClientCorrelationId(Exchange exchange) {
        String existing = StringUtils.trimToNull(
                exchange.getProperty(
                        Message.CLIENT_CORRELATION_ID,
                        String.class
                )
        );
        String inbound = StringUtils.trimToNull(
                exchange.getMessage().getHeader(
                        SCM_PARAMETER_CLIENT_CORRELATION_ID,
                        String.class
                )
        );
        if (existing != null && inbound != null
                && !existing.equals(inbound)) {
            throw new IllegalArgumentException(
                    "Conflicting scmClientCorrelationId values in normalized "
                            + "input"
            );
        }
        String resolved = existing == null ? inbound : existing;
        if (resolved != null) {
            exchange.setProperty(Message.CLIENT_CORRELATION_ID, resolved);
        }
    }
}
