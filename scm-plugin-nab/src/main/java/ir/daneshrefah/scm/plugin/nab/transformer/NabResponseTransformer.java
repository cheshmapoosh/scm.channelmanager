package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.exception.InvalidProviderResponseException;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import ir.daneshrefah.scm.plugin.nab.provider.Bind;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
@RequiredArgsConstructor
@Service
public class NabResponseTransformer extends AbstractJsonTransformer {

    private final ObjectMapper objectMapper;

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        if (null == payload) {
            return null;
        }

        ObjectNode payloadTmp = null;
        try {
            payloadTmp = payload instanceof ObjectNode ? (ObjectNode) payload : (ObjectNode) objectMapper.readTree((String) payload);
        } catch (Exception e) {
            ExternalServiceProvider provider = null;
            ir.daneshrefah.scm.common.model.service.Service service = message.getHeader().getService();
            if (service instanceof ExternalService) {
                provider = ((ExternalService) service).getServiceProvider();
            }
            throw new InvalidProviderResponseException(service.getCode(), provider.getCode(), e);
        }
        JsonNode resultNab = payloadTmp.get("result");
        ArrayNode arrayResult = JsonNodeFactory.instance.arrayNode();
        ObjectNode objectResult = JsonNodeFactory.instance.objectNode();
        JsonNode result=JsonNodeFactory.instance.objectNode();
        if(Objects.isNull(resultNab) || "null".equals(resultNab.toString())){
            arrayResult.addNull();
           return arrayResult;
        }
        else if (resultNab.isArray()) {
            ArrayNode arrayNode= (ArrayNode) resultNab;
            for (JsonNode jsonNode : arrayNode) {
                Bind bind = new Bind((ObjectNode) jsonNode, metadata);
                ObjectNode binding = bind.response();
                arrayResult.add(binding);
            }
            result=arrayResult;

        }else {
            Bind bind = new Bind((ObjectNode) resultNab, metadata);
            ObjectNode binding = bind.response();
            result=binding;
        }
        return result;

//        try {
//            JsonNode jsonPayload = payload instanceof JsonNode ? (JsonNode) payload : objectMapper.readTree((String) payload);
//            jsonPayload = jsonPayload.get("result");
//            ObjectNode jsonMetadata = (ObjectNode) objectMapper.readTree(metadata);
//            JsonNode result = jsonPayload.isArray() ? objectMapper.createArrayNode() : objectMapper.createObjectNode();
//            if (jsonPayload.isArray()) {
//                for (JsonNode jsonNode : jsonPayload) {
//                    ((ArrayNode) result).add(createResponseItem(jsonMetadata, (ObjectNode) jsonNode));
//                }
//            } else {
//                result = createResponseItem(jsonMetadata, (ObjectNode) jsonPayload);
//            }
//            return result;
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }

    private ObjectNode createResponseItem(ObjectNode metadata, ObjectNode payload) {
        ObjectNode result = objectMapper.createObjectNode();

        metadata.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            JsonNode sourceValue = payload.get(value.get("propertyName").asText());
            if (sourceValue.isTextual()) {
                result.put(key, sourceValue.asText().trim());
            } else {
                result.set(key, sourceValue);
            }
        });
        return result;
    }

    private static void modifyJsonNode(JsonNode node, JsonNode payload) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                if (fieldValue.isObject() || fieldValue.isArray()) {
                    // Recursively traverse nested objects or arrays
                    modifyJsonNode(fieldValue, payload);
                } else if (fieldValue.isTextual() && fieldValue.textValue().startsWith("$")) {
                    String propertyName = fieldValue.textValue().substring(2, fieldValue.textValue().length() - 1);
                    String newValue = payload.get(propertyName).textValue();
                    objectNode.put(fieldName, newValue);
                }
            });
        } else if (node.isArray()) {
            // Handle JSON arrays if needed
            // You can iterate through elements and recursively modify them
            for (JsonNode element : node) {
                modifyJsonNode(element, payload);
            }
        }
    }

    // Helper method to add a parameter object to the array
    private static void addParameter(ArrayNode parameters, String name, String value) {
        ObjectNode parameter = parameters.addObject();
        parameter.put("name", name);
        parameter.put("value", value);
    }
}
