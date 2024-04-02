package ir.daneshrefah.scm.plugin.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Iterator;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-27
 */
public class JSONConverter {

    private static final String OBJECT_SIGN = "$";
    private final ConverterDictionary dictionary;

    private JSONConverter(ConverterDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public ObjectNode convert(ObjectNode root) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        Iterator<String> fieldNames = root.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            BaseJsonNode fieldValue = (BaseJsonNode) root.get(fieldName);

            if (fieldValue.isObject() && fieldName.startsWith(OBJECT_SIGN)) {

                node.set(StringUtils.removeStart(fieldName, OBJECT_SIGN), extractValue((ObjectNode) fieldValue));

            } else if (fieldValue.isObject()) {
                ObjectNode traverse = convert((ObjectNode) fieldValue);
                if (!traverse.isEmpty()) node.set(fieldName, traverse);

            } else if (fieldValue.isTextual() || fieldValue.isNumber()) {
                node.set(fieldName, fieldValue);
            } else if (fieldValue.isArray()) {
                ArrayNode arrayNode = (ArrayNode) fieldValue;
                ArrayNode arrayResult = JsonNodeFactory.instance.arrayNode(); // Create an array to accumulate results
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode arrayElement = arrayNode.get(i);
                    BaseJsonNode traverse = convert((ObjectNode) arrayElement);
                    arrayResult.add(traverse);
                }
                if (arrayResult != null) {
                    node.putIfAbsent(fieldName, arrayResult);
                }
            } else {
                throw new RuntimeException("JsonNode root represents a single value field ");
            }
        }
        return node;
    }

    private JsonNode extractValue(ObjectNode fieldValue) {
        ConverterDictionary.ParameterDefinition definition = new ConverterDictionary.ParameterDefinition();
        definition.setFromValue(fieldValue.get("fromValue").asText());
        definition.setType(fieldValue.has("type") ? fieldValue.get("type").asText() : "string");
        definition.setLength(fieldValue.has("length") ? fieldValue.get("length").asInt() : null);
        definition.setConverter(fieldValue.has("convertor") ? fieldValue.get("convertor").asText() : null);
        definition.setMandatory(fieldValue.has("isMandatory") ? fieldValue.get("isMandatory").asBoolean() : false);
        return dictionary.convert(definition);
    }

    public static JSONConverter getInstance(ConverterDictionary dictionary) {
        return new JSONConverter(dictionary);
    }

}
