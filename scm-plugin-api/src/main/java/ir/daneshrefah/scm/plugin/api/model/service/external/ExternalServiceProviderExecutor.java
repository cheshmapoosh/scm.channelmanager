package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
public interface ExternalServiceProviderExecutor {

    public JsonNode execute(Message message, Service service);

}
