package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.utils.ConverterDictionary;
import ir.daneshrefah.scm.plugin.api.utils.JSONConverter;
import ir.daneshrefah.scm.plugin.api.utils.MessageConverterDictionary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
@RequiredArgsConstructor
@Service
public class NabRequestTransformer extends AbstractTransformer {

    private final ObjectMapper objectMapper;

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        if (metadata.has("rq")) {
            ObjectNode requestRoot = (ObjectNode) metadata.get("rq");
            ConverterDictionary dictionary = new MessageConverterDictionary(message);
            JSONConverter converter = JSONConverter.getInstance(dictionary);
            return converter.convert(requestRoot);
        }
        return null;

//        Bind bind=new Bind((ObjectNode) payload,metadata);
//        return bind.request();
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
