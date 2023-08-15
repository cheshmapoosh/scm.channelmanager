package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */

@Service
public class TerminalListService extends AbstractJavaService {

    @Override
    protected Object internalExecute(ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        ObjectNode objPayload = (ObjectNode) payload;
        String cardNo = null;
        if (objPayload.has("cardNo")) {
            cardNo = objPayload.get("cardNo").asText();
        }
//        throw new RuntimeException("invalid login");
        return 11111111L;
    }
}
