package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.config.SensitiveDataDecryptConfig;
import ir.daneshrefah.scm.core.config.SensitiveFieldConfig;
import ir.daneshrefah.scm.core.services.crypto.SensitiveDataDecryptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveDataDecryptPlugin implements PluginHandler {

    private static final String HEADER_PREFIX = "header:";
    private static final String BODY_PREFIX = "body:";
    private static final String APPLICATION_JSON_UTF8 = "application/json; charset=UTF-8";

    private final SensitiveDataDecryptService sensitiveDataDecryptService;
    private final ObjectMapper objectMapper;

    @Override
    public PluginType getType() {
        return PluginType.FILTER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {

    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) {
        SensitiveDataDecryptConfig config = resolveConfig(pluginDetail);


        if (config == null || !config.isEnabled()) {
            return;
        }

        List<SensitiveFieldConfig> fields = config.getFields();

        if (fields == null || fields.isEmpty()) {
            log.debug("SensitiveDataDecryptPlugin enabled but no fields configured");
            return;
        }

        Message message = exchange.getMessage();

        for (SensitiveFieldConfig field : fields) {
            decryptField(message, field, config);
        }


    }

    private SensitiveDataDecryptConfig resolveConfig(PluginDetail pluginDetail) {
        if (pluginDetail == null) {
            log.debug("SensitiveDataDecryptPlugin skipped because pluginDetail is null");
            return null;
        }


        Map<String, ?> rawConfig = pluginDetail.getConfig();

        if (rawConfig == null || rawConfig.isEmpty()) {
            log.debug("SensitiveDataDecryptPlugin skipped because database config is empty plugin={}", pluginDetail.getName());
            return null;
        }

        try {
            return objectMapper.convertValue(rawConfig, SensitiveDataDecryptConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert sensitiveDataDecryptPlugin database config. plugin=" + pluginDetail.getName(), e);
        }


    }

    private void decryptField(Message message, SensitiveFieldConfig field, SensitiveDataDecryptConfig config) {
        if (field == null || StringUtils.isBlank(field.getName())) {
            return;
        }


        String encryptedValue = findFirstValue(message, field.getSources());

        if (StringUtils.isBlank(encryptedValue)) {
            if (field.isRequired()) {
                throw new IllegalArgumentException("Sensitive encrypted field is required: " + field.getName());
            }

            log.debug("Sensitive field skipped because source value is empty name={}", field.getName());
            return;
        }

        String decryptedValue;

        try {
            decryptedValue = sensitiveDataDecryptService.decrypt(encryptedValue);
        } catch (Exception e) {
            if (config.isFailOnDecryptError()) {
                throw new IllegalStateException("Could not decrypt sensitive field: " + field.getName(), e);
            }

            log.warn("Could not decrypt sensitive field name={}", field.getName(), e);
            return;
        }

        if (StringUtils.isBlank(decryptedValue)) {
            if (field.isRequired()) {
                throw new IllegalArgumentException("Decrypted sensitive field is empty: " + field.getName());
            }

            log.debug("Sensitive field decrypted as empty name={}", field.getName());
            return;
        }

        validateDecryptedValue(field, decryptedValue);
        writeTargets(message, field.getTargets(), decryptedValue);

        log.info("Sensitive field decrypted successfully name={} targetCount={}", field.getName(), field.getTargets() != null ? field.getTargets().size() : 0);


    }

    private String findFirstValue(Message message, List<String> sources) {
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

    private String readValue(Message message, String location) {
        if (StringUtils.isBlank(location)) {
            return null;
        }


        if (location.startsWith(HEADER_PREFIX)) {
            String headerName = location.substring(HEADER_PREFIX.length());
            return message.getHeader(headerName, String.class);
        }

        if (location.startsWith(BODY_PREFIX)) {
            String path = location.substring(BODY_PREFIX.length());
            JsonNode body = resolveBodyAsJsonNode(message);
            Object value = getBodyPath(body, path);
            return value == null ? null : String.valueOf(value);
        }

        throw new IllegalArgumentException("Unsupported sensitive source location: " + location);


    }

    private void writeTargets(Message message, List<String> targets, String value) {
        if (targets == null || targets.isEmpty()) {
            return;
        }


        for (String target : targets) {
            writeValue(message, target, value);
        }


    }

    private void writeValue(Message message, String location, String value) {
        if (StringUtils.isBlank(location)) {
            return;
        }


        if (location.startsWith(HEADER_PREFIX)) {
            String headerName = location.substring(HEADER_PREFIX.length());
            message.setHeader(headerName, value);
            return;
        }

        if (location.startsWith(BODY_PREFIX)) {
            String path = location.substring(BODY_PREFIX.length());

            ObjectNode body = resolveBodyAsObjectNode(message);
            setBodyPath(body, path, value);
            writeJsonBodyAsInputStream(message, body);
            return;
        }

        throw new IllegalArgumentException("Unsupported sensitive target location: " + location);


    }

    private JsonNode resolveBodyAsJsonNode(Message message) {
        Object body = message.getBody();


        switch (body) {
            case null -> {
                ObjectNode empty = objectMapper.createObjectNode();
                writeJsonBodyAsInputStream(message, empty);
                return empty;
            }
            case JsonNode jsonNode -> {
                return jsonNode;
            }
            case String text -> {
                if (StringUtils.isBlank(text)) {
                    ObjectNode empty = objectMapper.createObjectNode();
                    writeJsonBodyAsInputStream(message, empty);
                    return empty;
                }

                try {
                    JsonNode jsonNode = objectMapper.readTree(text);
                    writeJsonBodyAsInputStream(message, jsonNode);
                    return jsonNode;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not parse String body as JSON", e);
                }
            }
            case byte[] bytes -> {
                try {
                    JsonNode jsonNode = objectMapper.readTree(bytes);
                    writeJsonBodyAsInputStream(message, jsonNode);
                    return jsonNode;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not parse byte[] body as JSON", e);
                }
            }
            case InputStream inputStream -> {
                try {
                    JsonNode jsonNode = objectMapper.readTree(inputStream);

                    writeJsonBodyAsInputStream(message, jsonNode);
                    return jsonNode;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not parse InputStream body as JSON", e);
                }
            }
            default -> {
            }
        }

        try {
            JsonNode jsonNode = objectMapper.valueToTree(body);
            writeJsonBodyAsInputStream(message, jsonNode);
            return jsonNode;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert request body to JsonNode. bodyType=" + body.getClass().getName(), e);
        }


    }

    private ObjectNode resolveBodyAsObjectNode(Message message) {
        JsonNode body = resolveBodyAsJsonNode(message);


        if (body instanceof ObjectNode objectNode) {
            return objectNode;
        }

        throw new IllegalArgumentException("Request body must be JSON object to write sensitive field. bodyNodeType=" + body.getNodeType());


    }

    private Object getBodyPath(JsonNode body, String path) {
        if (body == null || body.isNull() || StringUtils.isBlank(path)) {
            return null;
        }


        JsonNode current = body;

        for (String part : path.split("\\.")) {
            if (current == null || current.isNull() || !current.isObject()) {
                return null;
            }

            current = current.get(part);
        }

        if (current == null || current.isNull()) {
            return null;
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

    private void setBodyPath(ObjectNode body, String path, String value) {
        if (body == null || StringUtils.isBlank(path)) {
            return;
        }


        String[] parts = path.split("\\.");
        ObjectNode current = body;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            JsonNode next = current.get(part);

            if (next instanceof ObjectNode nextObject) {
                current = nextObject;
            } else {
                ObjectNode created = objectMapper.createObjectNode();
                current.set(part, created);
                current = created;
            }
        }

        current.put(parts[parts.length - 1], value);


    }

    private void writeJsonBodyAsInputStream(Message message, JsonNode body) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(body);


            message.setBody(new ByteArrayInputStream(jsonBytes));
            message.setHeader(Exchange.CONTENT_TYPE, APPLICATION_JSON_UTF8);
            message.setHeader(Exchange.CONTENT_LENGTH, jsonBytes.length);

        } catch (Exception e) {
            throw new IllegalStateException("Could not write JSON body as InputStream", e);
        }


    }

    private void validateDecryptedValue(SensitiveFieldConfig field, String decryptedValue) {
        if (StringUtils.isBlank(field.getValidateRegex())) {
            return;
        }


        if (!decryptedValue.matches(field.getValidateRegex())) {
            throw new IllegalArgumentException("Decrypted sensitive field format is invalid: " + field.getName());
        }


    }


}
