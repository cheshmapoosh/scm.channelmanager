package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogUtils {

    private static final ObjectMapper objectMapper;

    @Value("#{${scm.log.sensitive.fields:{password:'****', pwd:'****', pass:'****', passwordConfirm:'****', oldPassword:'****', newPassword:'****'}}}")
    private Map<String, String> SENSITIVE_FIELDS_MAP;

    @Getter
    private static LogUtils instance;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @PostConstruct
    public void init() {
        instance = this;
    }

    public String getRequestBody(HttpServletRequest request) {
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
            log.error("Exception occurred while getting request body {}", rawBody, e);
        }
        return rawBody;
    }

    public String getMessage(Exchange exchange) {
        if (exchange.getIn() == null) {
            String body = exchange.getIn().getBody(String.class);
            try {
                if (StringUtils.isNotBlank(body)) {
                    JsonNode jsonNode = objectMapper.readTree(body);
                    JsonNode sanitized = removeSensitiveFields(jsonNode);
                    return objectMapper.writeValueAsString(sanitized);
                }
            } catch (JsonProcessingException e) {
                log.error("Exception occurred while getting message {}", body, e);
            }
        }
        return "";
    }

    private JsonNode removeSensitiveFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (SENSITIVE_FIELDS_MAP.containsKey(field.getKey())) {
                    field.setValue(objectMapper.getNodeFactory().textNode(SENSITIVE_FIELDS_MAP.get(field.getKey())));
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
            log.error("Exception occurred while response body {}", inputArgs, e);
        }
        return "";
    }
}