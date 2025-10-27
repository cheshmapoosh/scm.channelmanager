package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class LogUtils {

    private static final ObjectMapper objectMapper;
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList("password", "pwd", "pass", "passwordConfirm", "oldPassword", "newPassword");

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String getRequestBody(HttpServletRequest request) {
        String rawBody = null;
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    rawBody = new String(content, StandardCharsets.UTF_8);
                    JsonNode jsonNode = objectMapper.readTree(rawBody);
                    JsonNode sanitized = removeSensitiveFields(jsonNode);
                    return objectMapper.writeValueAsString(sanitized);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return rawBody;
        }
        return rawBody;
    }

    static String getMessage(Exchange exchange) {
        if (exchange.getIn() == null) {
            String body = exchange.getIn().getBody(String.class);
            try {
                if (StringUtils.isNotBlank(body)) {
                    JsonNode jsonNode = objectMapper.readTree(body);
                    JsonNode sanitized = removeSensitiveFields(jsonNode);
                    return objectMapper.writeValueAsString(sanitized);
                }
            } catch (JsonProcessingException e) {
                log.error("exception while parsing message body:");
                return body;
            }
        }
        return "";
    }

    private static JsonNode removeSensitiveFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (SENSITIVE_FIELDS.contains(field.getKey())) {
                    fields.remove();
                } else {
                    removeSensitiveFields(field.getValue());
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                removeSensitiveFields(arrayElement);
            }
        }
        return node;
    }

    public static String getResponseBody(Object inputArgs) {
        try {
            return objectMapper.writeValueAsString(inputArgs);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }
}