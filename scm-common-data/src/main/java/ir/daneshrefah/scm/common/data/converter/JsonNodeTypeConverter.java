package ir.daneshrefah.scm.common.data.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
@Converter
public class JsonNodeTypeConverter implements AttributeConverter<JsonNode, String> {

    private final ObjectMapper objectMapper;

    public JsonNodeTypeConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String convertToDatabaseColumn(JsonNode value) {
        if (null == value || value.isNull() || value.isEmpty()) {
            return null;
        }
        return value.asText();
    }

    @Override
    public JsonNode convertToEntityAttribute(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            return objectMapper.valueToTree(value);
        }
    }

//    private ObjectMapper getObjectMapper() {
//        return ApplicationConfig.getObjectMapperInstance();
//    }

}