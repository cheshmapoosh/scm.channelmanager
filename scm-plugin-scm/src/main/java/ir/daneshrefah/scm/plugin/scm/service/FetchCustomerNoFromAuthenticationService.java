package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */

@Service("fetchCustomerNoFromAuthenticationService")
public class FetchCustomerNoFromAuthenticationService extends AbstractJavaService {
    public FetchCustomerNoFromAuthenticationService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @Override
    protected Object internalExecute(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        ObjectNode objPayload = (ObjectNode) payload;
        String cardNo = null;
        if (objPayload.has("cardNo")) {
            cardNo = objPayload.get("cardNo").asText();
        }
//        throw new RuntimeException("invalid login");
//        if (null != message.getHeader().getAuthentication())
//            return "IR970130100000000000001399";
        return "45454545";
    }
}
