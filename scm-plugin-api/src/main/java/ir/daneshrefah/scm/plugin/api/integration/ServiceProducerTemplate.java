package ir.daneshrefah.scm.plugin.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public interface ServiceProducerTemplate {

    public Message callService(Service service, Message message);

    public Message callService(String serviceCode, JsonNode payload);
    public Message callServiceWithException(String serviceCode, JsonNode payload);

    public Message callService(String serviceCode);

}
