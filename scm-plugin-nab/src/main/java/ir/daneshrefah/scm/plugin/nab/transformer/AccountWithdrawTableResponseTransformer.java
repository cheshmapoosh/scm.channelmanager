package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-22
 */
@Service
public class AccountWithdrawTableResponseTransformer extends AbstractJsonTransformer {

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        if (null == payload || !(payload instanceof JsonNode) || ((JsonNode) payload).isNull() || !((JsonNode) payload).isArray()) {
            return NullNode.getInstance();
        }
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = (ArrayNode) payload;
        Map<String, ArrayNode> groups = new HashMap<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            addNodeToGroup(mapper, groups, node);
//            String key = node.get("SIGNNO").asText();
//            groups.computeIfAbsent(key, k -> mapper.createArrayNode()).add(node);
        }
        return mapper.createArrayNode().addAll(groups.values());
    }

    private void addNodeToGroup(ObjectMapper mapper, Map<String, ArrayNode> groups, JsonNode newNode) {
        String key = newNode.get("SIGNNO").asText();
        if (!groups.containsKey(key)) {
            groups.put(key, mapper.createArrayNode());
        }
        ArrayNode arrayNode = groups.get(key);
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            if (node.isObject() && node.has("customerNo") && node.get("customerNo").asText().equals(newNode.get("CUSTOMERID").asText()))
                return;
        }
        arrayNode.add(createSignNode(mapper, newNode));
    }

    private JsonNode createSignNode(ObjectMapper mapper, JsonNode node) {
        ObjectNode result = mapper.createObjectNode();
        result.put("customerNo", node.get("CUSTOMERID").asText());
        result.put("nationalId", node.get("NATIONALID").asText());
        result.put("subOrganizationCode", node.get("SUBORG").asText());
        result.put("title", node.get("FIRSTNAME").asText() + " " + node.get("LASTNAME").asText());
        return result;
    }

}
