package ir.daneshrefah.scm.core.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProviderMetadata;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdollahi
 * @version 1.0
 * @since 2024-03-30
 */
@Converter
public class ExternalServiceProviderMetadataConverter implements AttributeConverter<ExternalServiceProviderMetadata, String> {

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(ExternalServiceProviderMetadata metaData) {
        if (null == metaData) {
            return null;
        }
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        if (StringUtils.isNotEmpty(metaData.getEndpoint())) {
            result.put("endpoint", metaData.getEndpoint());
        }
        if (null != metaData.getConnectTimeout()) {
            result.put("connectTimeout", metaData.getConnectTimeout());
        }
        if (null != metaData.getResponseTimeout()) {
            result.put("responseTimeout", metaData.getResponseTimeout());
        }
        if (null != metaData.getSoTimeout()) {
            result.put("soTimeout", metaData.getSoTimeout());
        }
        for (Map.Entry<String, Object> entry : metaData.getAdditionalParams().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                result.put(entry.getKey(), (Integer) entry.getValue());
            } else if (value instanceof Long) {
                result.put(entry.getKey(), (Long) entry.getValue());
            } else if (value instanceof Float) {
                result.put(entry.getKey(), (Float) entry.getValue());
            } else if (value instanceof String) {
                result.put(entry.getKey(), (String) entry.getValue());
            }
        }
        return result.toString();
    }

    @Override
    @SneakyThrows
    public ExternalServiceProviderMetadata convertToEntityAttribute(String strMetadata) {
        if (StringUtils.isEmpty(strMetadata)) {
            return null;
        }
        JsonNode jsonMetadata = getObjectMapper().readTree(strMetadata);
        ExternalServiceProviderMetadata metadata = new ExternalServiceProviderMetadata();
        jsonMetadata.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();
            if (fieldName.equalsIgnoreCase("endpoint")) {
                metadata.setEndpoint(fieldValue.asText());
            } else if (fieldName.equalsIgnoreCase("connectTimeout")) {
                metadata.setConnectTimeout(fieldValue.asInt());
            } else if (fieldName.equalsIgnoreCase("responseTimeout")) {
                metadata.setResponseTimeout(fieldValue.asInt());
            } else if (fieldName.equalsIgnoreCase("soTimeout")) {
                metadata.setSoTimeout(fieldValue.asInt());
            } else {
                metadata.addParam(fieldName, fieldValue);
            }
        });
        return metadata;
    }

    private ObjectMapper getObjectMapper() {
        return ApplicationConfig.getObjectMapperInstance();
    }
}