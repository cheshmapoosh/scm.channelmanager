package ir.daneshrefah.scm.process.service.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.process.model.process.ProcessMetadata;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.*;

@Component
@AllArgsConstructor
public class ProcessMetadataExtractor {
    private BpmnExtensionExtractor bpmnExtensionExtractor;

    public <T extends ModelElementInstance> ProcessMetadata extractProcessMetadata(ProcessDefinition processDefinition, Class<T> clazz) throws Exception {
        Map<String, String> extensionProperties = getExtensionProperties(processDefinition,clazz);
        ProcessMetadata processMetadata = new ProcessMetadata();
        for (Map.Entry<String, String> entry : extensionProperties.entrySet()) {
            String propertyKey = entry.getKey();
            String propertyValue = entry.getValue();
            validateMetadata(propertyValue, propertyKey);
            switch (propertyKey) {
                case JSON_SCHEMA -> processMetadata.setStartValidationSchema(propertyValue);
                case JS_VALIDATION -> processMetadata.setStartValidationScript(propertyValue);
                case CONVERTORS -> processMetadata.setInputConverters(convertToMap(propertyValue));
                case CANCEL_PROCESS_USER -> processMetadata.setCancelAuthorizedUsers(Arrays.asList(propertyValue.split(",")));
                case CANCEL_PROCESS_ROLE -> processMetadata.setCancelAuthorizedAuthorities(Arrays.asList(propertyValue.split(",")));
                default -> throw new IllegalArgumentException("Unexpected property key: " + propertyKey);
            }
        }
        return processMetadata;
    }

    private Map<String, String> convertToMap(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    private void validateMetadata(String value, String fieldName) throws Exception {
        if (StringUtils.isEmpty(value) || StringUtils.isBlank(value)) {
            throw new Exception("Missing " + fieldName); //TODO Change exception handling
        }
    }
    private <T extends ModelElementInstance> Map<String, String> getExtensionProperties(ProcessDefinition processDefinition,Class<T> clazz) {
        return bpmnExtensionExtractor.getExtensionProperties(processDefinition, clazz);
    }
}
