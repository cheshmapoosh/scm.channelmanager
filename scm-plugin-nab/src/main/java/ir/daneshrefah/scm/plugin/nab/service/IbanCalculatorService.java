package ir.daneshrefah.scm.plugin.nab.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.ServiceComponentExecutor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
public class IbanCalculatorService extends AbstractJavaService {

    public IbanCalculatorService(ServiceComponentExecutor serviceComponentExecutor) {
        super(serviceComponentExecutor);
    }

    @Override
    protected void internalExecute(Message message) {
        Object componentPayload = "{\"iban\": \"IR970130100000000000001399\",\"paymentCode\": \"124\"}";
        callServiceComponent("NAB", "IBAN_INQ", message, componentPayload);

        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode payload = nodeFactory.objectNode();
        payload.put("iban", "IR213123123123");
        message.setPayload(payload);
        message.setStatus(Status.SC_SUCCESS);
        System.out.println("");
    }

}
