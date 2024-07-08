package ir.daneshrefah.scm.process.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import ir.daneshrefah.scm.process.service.util.convertor.ConvertorFunction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class JsonTransformationUtil {
    private final ConvertorFunction convertorFunction;

    public void transformBusinessData(JsonNode jsonNode, Map<String, String> inputConverters) {
        if (Objects.isNull(jsonNode) || jsonNode.isEmpty() || Objects.isNull(inputConverters) || inputConverters.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : inputConverters.entrySet()) {
            String propertyKey = entry.getKey();
            String convertorMethodName = entry.getValue();
            transformJsonNodeHelper(jsonNode, propertyKey.split("\\."), 0, convertorMethodName);
        }
    }

    private void transformJsonNodeHelper(JsonNode node, String[] keys, int index, String methodToInvoke) {
        if (index < keys.length) {
            String key = keys[index];
            if (node.isObject()) {
                JsonNode nextNode = node.get(key);
                if (nextNode != null) {
                    transformJsonNodeHelper(nextNode, keys, index + 1, methodToInvoke);
                }
            } else if (node.isArray()) {
                ArrayNode arrayNode = (ArrayNode) node;
                for (JsonNode element : arrayNode) {
                    transformJsonNodeHelper(element, keys, index, methodToInvoke);
                }
            }
            if (index == keys.length - 1 && key.equals(keys[index])) {
                if (node.isObject()) {
                    ObjectNode objectNode = (ObjectNode) node;
                    objectNode.set(key, convertJsonNode(objectNode.get(key), methodToInvoke));
                } else if (node.isArray()) {
                    for (JsonNode element : node) {
                        if (element.isObject()) {
                            ObjectNode objectNode = (ObjectNode) element;
                            objectNode.set(key, convertJsonNode(objectNode.get(key), methodToInvoke));
                        }
                    }
                }
            }
        }
    }

    private JsonNode convertJsonNode(JsonNode jsonNode, String methodToInvoke) {
        Object data = invokeConversionMethod(jsonNode, methodToInvoke);
        ObjectMapper objectMapper = new ObjectMapper();
        if (data instanceof String) {
            return TextNode.valueOf((String) data);
        } else if (data instanceof Number) {
            return objectMapper.convertValue(data, JsonNode.class);
        } else if (data instanceof Boolean) {
            return objectMapper.convertValue(data, JsonNode.class);
        } else if (data instanceof JsonNode) {
            return (JsonNode) data;
        } else {
            return objectMapper.valueToTree(data);
        }
    }

    private Object invokeConversionMethod(JsonNode node, String methodToInvoke) {
        if (node.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            for (int i = 0; i < node.size(); i++) {
                JsonNode jsonNode = node.get(i);
                if (jsonNode.isObject()) {
                    list.add(convertorFunction.call(methodToInvoke, jsonNode));
                }
            }
            return list;
        } else if (node.isObject()) {
            return convertorFunction.call(methodToInvoke, node);
        }
        return null;
    }
}
