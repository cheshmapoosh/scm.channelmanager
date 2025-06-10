package ir.daneshrefah.scm.utils.string;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonPathFinder {
    private JsonPathFinder() {
    }

    public static JsonNode defaultNode (JsonNode node, String path) {
        return node.get(path);
    }

    public static String defaultAsText(JsonNode node, String path) {
        return defaultAsText(node, path, null);
    }

    public static String defaultAsText(JsonNode node, String path, String defaultValue) {
        JsonNode pathNode = node.get(path);
        if (pathNode == null || pathNode.isNull() || pathNode.isMissingNode()) {
            return defaultValue;
        }
        return pathNode.asText();
    }

    public static Integer defaultAsInteger(JsonNode node, String path) {
        return defaultAsInteger(node, path, null);
    }

    public static Integer defaultAsInteger(JsonNode node, String path, Integer defaultValue) {
        JsonNode pathNode = node.get(path);
        if (pathNode == null || pathNode.isNull() || pathNode.isMissingNode()) {
            return defaultValue;
        }
        return pathNode.asInt();
    }

    public static Long defaultAsLong(JsonNode node, String path) {
        return defaultAsLong(node, path, null);
    }

    public static Long defaultAsLong(JsonNode node, String path, Long defaultValue) {
        JsonNode pathNode = node.get(path);
        if (pathNode == null || pathNode.isNull() || pathNode.isMissingNode()) {
            return defaultValue;
        }
        return pathNode.asLong(defaultValue);
    }

    public static Boolean defaultAsBoolean(JsonNode node, String path) {
        return defaultAsBoolean(node, path, null);
    }
    public static Boolean defaultAsBoolean(JsonNode node, String path, Boolean defaultValue) {
        JsonNode pathNode = node.get(path);
        if (pathNode == null || pathNode.isNull() || pathNode.isMissingNode()) {
            return defaultValue;
        }
        return pathNode.asBoolean();
    }


    public static List<String> findPropertiesOnJsonNode(String propertyPath, JsonNode payload) {
        if (payload == null || StringUtils.isBlank(propertyPath)) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        if (payload.has(propertyPath)) {
            JsonNode jsonNode = payload.get(propertyPath);
            if (!jsonNode.isArray() && !jsonNode.isObject()) {
                result.add(payload.get(propertyPath).toString());
                return result;
            }
        }
        JsonNode root = navigateToNode(payload, propertyPath);
        if (root == null) {
            return Collections.emptyList();
        }
        String targetProperty = extractLastSegment(propertyPath);
        if (root.isArray()) {
            for (JsonNode node : root) {
                addPropertyValueToList(node, targetProperty, result);
            }
        } else if (root.isObject()) {
            addPropertyValueToList(root, targetProperty, result);
        } else {
            result.add(root.asText());
        }
        return result;
    }

    private static JsonNode navigateToNode(JsonNode node, String path) {
        String[] steps = StringUtils.split(path, ".");
        for (int i = 0; i < steps.length - 1; i++) {
            if (node == null) return null;
            node = node.path(steps[i]);
        }
        return node;
    }

    private static void addPropertyValueToList(JsonNode node, String propertyName, List<String> result) {
        if (node.has(propertyName)) {
            JsonNode propertyNode = node.get(propertyName);
            if (propertyNode.isArray()) {
                propertyNode.forEach(item -> result.add(item.asText()));
            } else {
                result.add(propertyNode.asText());
            }
        }
    }

    private static String extractLastSegment(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex >= 0 ? path.substring(lastDotIndex + 1) : path;
    }
}
