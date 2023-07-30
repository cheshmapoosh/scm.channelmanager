package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class IbanInqResponseTransformer extends AbstractTransformer {


    @Override
    public Object transform(Object inputSchema, Object outputSchema, Message message, String metadata) {
        Object response = message.getMessageComponent().getPayload();
        if (null == response) {
            return null;
        }
        if (response instanceof String && StringUtils.isEmpty((String) response))
            return null;
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode result = objectMapper.createArrayNode();
        JsonNode responseNode = null;
        if (response instanceof JsonNode)
            responseNode = (JsonNode) response;
        if (response instanceof String) {
            try {
                responseNode = objectMapper.readTree((String) response);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        for (JsonNode element : responseNode.get("result")) {
            // Get the properties from the current element
//            "SRLACC":1399.0,
//            "STATUSX":1,
//            "GENERAL":407,
//            "RANGEID":0,
//            "IBANVALUE":"IR970130100000000000001399",
            String firstName = element.get("FNAME").asText();
            String lastName = element.get("LNAME").asText();
            Integer customerTypeCode = element.get("CUSTOMERTYPE").asInt();
            Integer accountTypeCode = element.get("ACCOUNTYPE").asInt();
            Integer accountStatusCode = element.get("ACCOUNSTATUS").asInt();
//            "TASHILAT":0,
//            "":2,
//            "RQID":"15975368"

            // Create a new ObjectNode with modified property names
            ObjectNode modifiedElement = objectMapper.createObjectNode();
            modifiedElement.put("firstName", firstName);
            modifiedElement.put("lastname", lastName);
            modifiedElement.put("customerTypeCode", customerTypeCode);
            modifiedElement.put("accountTypeCode", accountTypeCode);
            modifiedElement.put("accountStatusCode", accountTypeCode);

            // Add the modified element to the new array
            result.add(modifiedElement);
        }
        return result;
    }
    
}
