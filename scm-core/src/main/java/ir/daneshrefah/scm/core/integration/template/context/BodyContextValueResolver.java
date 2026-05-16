package ir.daneshrefah.scm.core.integration.template.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BodyContextValueResolver implements ContextValueResolver {
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String key) {
        return StringUtils.startsWithIgnoreCase(key, "body.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String name = StringUtils.removeStartIgnoreCase(key, "body.");
        JsonNode body = bodyAsJsonNode(exchange.getIn().getBody());
        JsonNode value = resolvePath(body, name);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isNumber()) {
            return value.numberValue();
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        return objectMapper.convertValue(value, Object.class);
    }

    private JsonNode bodyAsJsonNode(Object body) {
        if (body == null) {
            return objectMapper.nullNode();
        }
        if (body instanceof JsonNode jsonNode) {
            return jsonNode;
        }
        if (body instanceof String text) {
            if (StringUtils.isBlank(text)) {
                return objectMapper.nullNode();
            }
            try {
                return objectMapper.readTree(text);
            } catch (Exception e) {
                throw new IllegalArgumentException("Body template variable source must be a JSON object", e);
            }
        }
        return objectMapper.valueToTree(body);
    }

    private JsonNode resolvePath(JsonNode source, String path) {
        if (source == null || StringUtils.isBlank(path)) {
            return source;
        }
        JsonNode current = source;
        for (String segment : path.split("\\.")) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return MissingNode.getInstance();
            }
            current = current.path(segment);
        }
        return current;
    }
}
