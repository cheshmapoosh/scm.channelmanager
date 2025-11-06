package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class AtpsHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * Fixes string length by trimming or padding right with spaces.
     * @param value  Input value (nullable)
     * @param length Desired fixed length
     * @return String of exact length (padded or trimmed)
     */
    public static String fix(Object value, int length) {
        if (value == null) {
            return String.format("%-" + length + "s", "");
        }

        String str = value.toString().trim();
        if (str.length() > length) {
            str = str.substring(0, length);
        }
        return String.format("%-" + length + "s", str);
    }

    /**
     * Parses JSON string into a Map.
     * @param jsonString JSON input (String)
     * @return Map representation of JSON
     */
    public static Map<String, Object> toMap(String jsonString) {
        try {
            return mapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON input: " + jsonString, e);
        }
    }

    /**
     * Converts JSON string to fixed-length plaintext record.
     * Field lengths: command=2, service=2, dateTime=14,
     * cmUserId=10, cmPassword=10, rquid=16 (sum=58)
     */
    public static String jsonToPlainText(String jsonString) {
        Map<String, Object> json = toMap(jsonString);

        String command    = fix(json.get("command"), 2);
        String service    = fix(json.get("service"), 2);
        String dateTime   = fix(json.get("dateTime"), 14);
        String cmUserId   = fix(json.get("cmUserId"), 10);
        String cmPassword = fix(json.get("cmPassword"), 10);
        String rquid      = fix(json.get("rquid"), 16);

        return command + service + dateTime + cmUserId + cmPassword + rquid;
    }

    public static ObjectNode parseFixedWidthResponse(String response) {
        if (response == null || response.length() < 61) {
            throw new IllegalArgumentException("Response too short: " + response);
        }

        int idx = 0;
        String actionCode         = response.substring(idx, idx + 5); idx += 5;
        String command            = response.substring(idx, idx + 2); idx += 2;
        String service            = response.substring(idx, idx + 2); idx += 2;
        String dateTime           = response.substring(idx, idx + 14); idx += 14;
        String referenceNo        = response.substring(idx, idx + 16); idx += 16;
        String paymentCode        = response.substring(idx, idx + 6); idx += 6;
        String paymentDescription = response.substring(idx, idx + 16); idx += 16;

        ObjectNode node = mapper.createObjectNode();
        node.put("actionCode", actionCode.trim());
        node.put("command", command.trim());
        node.put("service", service.trim());
        node.put("dateTime", dateTime.trim());
        node.put("referenceNo", referenceNo.trim());
        node.put("paymentCode", paymentCode.trim());
        node.put("paymentDescription", paymentDescription.trim());

        return node;
    }

    public static ObjectNode[] parseFixedWidthArray(String[] records) {
        if (records == null) {
            return new ObjectNode[0];
        }

        ObjectNode[] nodes = new ObjectNode[records.length];
        for (int i = 0; i < records.length; i++) {

            String record = records[i] != null ? String.format("%-61s", records[i]) : "                                                               ";
            nodes[i] = parseFixedWidthResponse(record);
        }

        return nodes;
    }
}
