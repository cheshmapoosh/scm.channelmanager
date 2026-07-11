package ir.daneshrefah.scm.core.integration.plugin.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PluginMessageValueReader {

    private static final String HEADER_PREFIX = "header:";
    private static final String BODY_PREFIX = "body:";

    private final ObjectMapper objectMapper;

    public String findFirstValue(Message message, List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        for (String source : sources) {
            String value = readValue(message, source);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    public String readValue(Message message, String location) {
        if (message == null || StringUtils.isBlank(location)) {
            return null;
        }
        if (location.startsWith(HEADER_PREFIX)) {
            String headerName = location.substring(HEADER_PREFIX.length());
            return message.getHeader(headerName, String.class);
        }
        if (location.startsWith(BODY_PREFIX)) {
            Object value = readBodyPath(message.getBody(), location.substring(BODY_PREFIX.length()));
            return value == null ? null : String.valueOf(value);
        }
        throw new IllegalArgumentException("Unsupported plugin source location: " + location);
    }

    private Object readBodyPath(Object body, String path) {
        if (body == null || StringUtils.isBlank(path)) {
            return null;
        }
        if (body instanceof Map<?, ?> map) {
            return readMapPath(map, path);
        }
        JsonNode jsonNode = bodyAsJson(body);
        return readJsonPath(jsonNode, path);
    }

    private Object readMapPath(Map<?, ?> map, String path) {
        Object current = map;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = currentMap.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private JsonNode bodyAsJson(Object body) {
        try {
            if (body instanceof JsonNode jsonNode) {
                return jsonNode;
            }
            if (body instanceof String text) {
                if (StringUtils.isBlank(text)) {
                    return objectMapper.createObjectNode();
                }
                return objectMapper.readTree(text);
            }
            if (body instanceof byte[] bytes) {
                return objectMapper.readTree(bytes);
            }
            if (body instanceof InputStream inputStream) {
                return objectMapper.readTree(inputStream);
            }
            return objectMapper.valueToTree(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read plugin message body as JSON", e);
        }
    }

    private Object readJsonPath(JsonNode node, String path) {
        JsonNode current = node;
        for (String part : path.split("\\.")) {
            if (current == null || current.isNull() || !current.isObject()) {
                return null;
            }
            current = current.get(part);
            if (current == null || current.isNull()) {
                return null;
            }
        }
        if (current.isTextual()) {
            return current.asText();
        }
        if (current.isNumber()) {
            return current.numberValue();
        }
        if (current.isBoolean()) {
            return current.asBoolean();
        }
        return current;
    }
}
