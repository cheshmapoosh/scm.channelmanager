package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Service
public class AppendLoggedInInfoTransformer extends AbstractTransformer {

    private ObjectMapper objectMapper;

    public AppendLoggedInInfoTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object internalTransform(Object payload, Message message, String metadata) {
        JsonNode result = null;
        if (null == message.getPayload() || message.getPayload().isNull()) {
            result = objectMapper.createObjectNode();
        } else {
            result = message.getPayload().deepCopy();
        }
        if (StringUtils.isEmpty(metadata))
            return result;
        try {
            ArrayNode jsonMetadata = (ArrayNode) objectMapper.readTree(metadata);
            for (JsonNode element : jsonMetadata) {
                // Access individual properties within each object
                String propertyName = element.get("propertyName").asText();
                String value = element.get("value").asText();

                if (StringUtils.equalsIgnoreCase("${customerNo}", value)) {
                    ((ObjectNode) result).put(propertyName, extractCustomerNo(message));
                } else if (StringUtils.equalsIgnoreCase("${username}", value)) {
                    ((ObjectNode) result).put(propertyName, extractUsername(message));
                }
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private String extractCustomerNo(Message message) {
        PersonProfile profile = message.getHeader().getPersonIdentifier();
        ir.daneshrefah.scm.common.model.service.Service service = message.getHeader().getService().getTerminalServiceAccess().getService();
        ExternalServiceProvider provider = service instanceof ExternalService ? ((ExternalService) service).getServiceProvider() : null;
        if (null == provider || !provider.isCustomerProvided() || null == profile) {
            return null;
        }
        Customer customer = profile.getCustomer(provider.getId());
        if (null == customer) {
            return null;
        }
        return customer.getCustomerNo();
//        return "11342422";
    }

    private String extractUsername(Message message) {
        if (null != message.getHeader().getAuthentication())
            return message.getHeader().getAuthentication().getName();//TODO username
        else
            return null;
    }
}
