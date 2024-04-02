package ir.daneshrefah.scm.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
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
            return getObjectMapper().readTree(value);
        } catch (JsonProcessingException e) {
            return getObjectMapper().valueToTree(value);
        }
    }

    private ObjectMapper getObjectMapper() {
        return ApplicationConfig.getObjectMapperInstance();
    }

}