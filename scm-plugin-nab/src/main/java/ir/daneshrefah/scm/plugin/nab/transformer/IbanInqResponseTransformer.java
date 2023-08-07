package ir.daneshrefah.scm.plugin.nab.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
@Component("ibanInqResponseTransformer")
public class IbanInqResponseTransformer extends AbstractTransformer {


    @Override
    public Object internalTransform(Object payload, Message message, String metadata) {
        if (null == payload) {
            return null;
        }
        if (payload instanceof String && StringUtils.isEmpty((String) payload))
            return null;
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode result = objectMapper.createArrayNode();
        JsonNode responseNode = null;
        if (payload instanceof JsonNode)
            responseNode = (JsonNode) payload;
        if (payload instanceof String) {
            try {
                responseNode = objectMapper.readTree((String) payload);
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
            String iban = element.get("IBANVALUE").asText();
            String account = element.get("SRLACC").asText();
            String status = element.get("STATUSX").asText();
            String firstName = element.get("FNAME").asText();
            String lastName = element.get("LNAME").asText();
            Integer customerTypeCode = element.get("CUSTOMERTYPE").asInt();
            Integer accountTypeCode = element.get("ACCOUNTYPE").asInt();
            Integer accountStatusCode = element.get("ACCOUNSTATUS").asInt();
//            "TASHILAT":0,
//            "RQID":"15975368"

            // Create a new ObjectNode with modified property names
            ObjectNode modifiedElement = objectMapper.createObjectNode();
            modifiedElement.put("iban", iban);
            modifiedElement.put("account", account);
            modifiedElement.put("status", status);
            modifiedElement.put("firstName", firstName);
            modifiedElement.put("lastname", lastName);
            modifiedElement.put("customerTypeCode", customerTypeCode);
            modifiedElement.put("accountTypeCode", accountTypeCode);
            modifiedElement.put("accountStatusCode", accountStatusCode);

            // Add the modified element to the new array
            result.add(modifiedElement);
        }
        return result;
    }
    
}
