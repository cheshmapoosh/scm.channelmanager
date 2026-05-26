package ir.daneshrefah.scm.provider.nab.codec;

import com.fasterxml.jackson.databind.JsonNode;

final class JsonNodeSupport {
    private JsonNodeSupport() {
    }

    static String text(JsonNode node, String fieldName) {
        JsonNode child = node == null ? null : node.get(fieldName);
        if (child == null || child.isNull() || child.isMissingNode()) {
            return null;
        }
        return child.asText();
    }

    static String textAt(JsonNode root, String path) {
        JsonNode node = root == null || path == null ? null : root.at(path);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    static String pointerForName(String name) {
        return "/" + name.replace("~", "~0").replace("/", "~1");
    }
}
