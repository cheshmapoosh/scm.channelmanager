package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Service
public class AppendPropTransformer extends AbstractTransformer {

    private JsonNode emptyJsonNode;
    private ObjectMapper objectMapper;

    public AppendPropTransformer(ObjectMapper objectMapper) {
//        ObjectMapper objectMapper = new ObjectMapper();
        this.objectMapper = objectMapper;
        emptyJsonNode = objectMapper.createObjectNode();
    }

    @Override
    public JsonNode internalTransform(Object payload, Message message, String metadata) {
        JsonNode result = message.getPayload().deepCopy();
        try {
            JsonNode jsonMetadata = objectMapper.readTree(metadata);
            if (jsonMetadata.isArray()) {
                // Iterate over the array elements
                for (JsonNode arrayElement : jsonMetadata) {
                    String sourceProperty = arrayElement.get("sourceProp").asText();
                    String targetProperty = arrayElement.get("targetProp").asText();

                    ((ObjectNode) result).put(targetProperty, extractPayloadValue(payload, sourceProperty));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String extractPayloadValue(Object payload, String sourceProperty) {
        String propertyValue = null;
        if (payload instanceof JsonNode) {
            JsonNode propertyValueNode = (JsonNode) payload;
            if (StringUtils.equalsIgnoreCase("body", sourceProperty)) {
                propertyValue = propertyValueNode.asText();
            } else {
                propertyValue = propertyValueNode.get(sourceProperty).asText();
            }
        }
        return propertyValue;
    }
}
