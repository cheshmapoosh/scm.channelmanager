package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class IbanInqRequestTransformer extends AbstractTransformer {

    @Override
    public Object transform(Object inputSchema, Object outputSchema, Message message, String metadata) {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectMapper mapper = new ObjectMapper();

        // Create the parameters array
        ArrayNode parameters = nodeFactory.arrayNode();
        addParameter(parameters, "P_TYPEX", "1");
        addParameter(parameters, "P_BIC", "1");
        addParameter(parameters, "P_IBAN", message.getPayload().get("iban").asText());
        addParameter(parameters, "P_RQID", message.getHeader().getCorrelationId());
        if (null != message.getPayload().get("paymentCode")) {
            addParameter(parameters, "P_PAYMENTCODE", message.getPayload().get("paymentCode").asText());
        } else {
            addParameter(parameters, "P_PAYMENTCODE", "");
        }

        // Create the main JSON object
        ObjectNode jsonObject = nodeFactory.objectNode();
        jsonObject.set("parameters", parameters);
        jsonObject.put("callType", "Reader");
        jsonObject.put("encoding", "ASCII");
        jsonObject.put("requestID", "RequestID");

//        String jsonString = jsonObject.toString();

        return jsonObject;
    }

    // Helper method to add a parameter object to the array
    private static void addParameter(ArrayNode parameters, String name, String value) {
        ObjectNode parameter = parameters.addObject();
        parameter.put("name", name);
        parameter.put("value", value);
    }
}
