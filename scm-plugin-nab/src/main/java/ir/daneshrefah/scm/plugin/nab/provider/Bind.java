package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.utils.string.StringUtils;


import java.util.*;


public class Bind {
    private final String metaDataJson;
    private final static String RQ = "rq";
    private final static String RS = "rs";
    private final ObjectNode body;

    public Bind(ObjectNode requestBody, String metadata) {
        this.body = requestBody;
        this.metaDataJson = metadata;
    }


    public ObjectNode request() {
        ObjectNode meta = jsonStringToJsonNode(this.metaDataJson);
        if (!meta.has(RQ)) {
            return this.body;
        }
        ObjectNode root = (ObjectNode) meta.get(RQ);
        return traverse(root, "");
    }

    public ObjectNode response() {
        ObjectNode meta = jsonStringToJsonNode(this.metaDataJson);
        if (!meta.has(RS)) {
            return this.body;
        }
        ObjectNode root = (ObjectNode) meta.get(RS);
        return traverse(root, "");
    }

    private ObjectNode traverse(ObjectNode root, String parentFieldName) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        Iterator<String> fieldNames = root.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            BaseJsonNode fieldValue = (BaseJsonNode) root.get(fieldName);

            if (fieldValue.isObject() && fieldName.startsWith("$")) {

                node.set(StringUtils.removeStart(fieldName, "$"), extractValue((ObjectNode) fieldValue));

            } else if (fieldValue.isObject()) {
                ObjectNode traverse = traverse((ObjectNode) fieldValue, fieldName);
                if (!traverse.isEmpty()) node.set(fieldName, traverse);

            } else if (fieldValue.isTextual() || fieldValue.isNumber()) {
                node.set(fieldName, fieldValue);
            } else if (fieldValue.isArray()) {
                ArrayNode arrayNode = (ArrayNode) fieldValue;
                ArrayNode arrayResult = JsonNodeFactory.instance.arrayNode(); // Create an array to accumulate results
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode arrayElement = arrayNode.get(i);
                    BaseJsonNode traverse = traverse((ObjectNode) arrayElement, fieldName);
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
        JsonNode fromValue = fieldValue.get("fromValue");
        JsonNode type = fieldValue.get("type");
        JsonNode length = fieldValue.get("length");
        JsonNode convertor = fieldValue.get("convertor");
        String text = fromValue.asText();
        JsonNode extraction = extraction(text);


        return extraction;
    }

    private JsonNode extraction(String text) {//todo about array in metadata
        boolean containsDot = text.contains(".");
        if (containsDot) {
            String[] split = text.split("\\.");
            JsonNode jsonNode = body.get(split[0]);
            for (int i = 1; i < split.length; i++) {
                String s = split[i];
                if (jsonNode.isArray()) {
                    ArrayNode arrayNode = (ArrayNode) jsonNode;
                    for (JsonNode node : arrayNode) {
                        jsonNode = node.get(s);
                    }

                } else if (jsonNode.isObject()) {
                    jsonNode = jsonNode.get(s);
                }
            }
            return jsonNode;
        } else
            return body.get(text);
    }


    private ObjectNode jsonStringToJsonNode(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return (ObjectNode) mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


}

