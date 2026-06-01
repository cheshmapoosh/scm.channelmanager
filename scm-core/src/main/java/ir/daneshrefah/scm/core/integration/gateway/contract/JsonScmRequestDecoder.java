package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
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
        Object body = exchange.getMessage().getBody();
        exchange.setProperty(Message.ORIGINAL_BODY, body);
        exchange.setProperty(Message.ORIGINAL_HEADERS, new LinkedHashMap<>(exchange.getMessage().getHeaders()));
        exchange.setProperty(Message.INTERNAL_MESSAGE, Message.builder()
                .header(Header.builder().build())
                .status(MessageStatus.SC_PROCESSING)
                .payload(toJsonNode(body))
                .build());
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
}
