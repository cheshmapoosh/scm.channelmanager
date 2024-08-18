package ir.daneshrefah.scm.plugin.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import org.springframework.lang.Nullable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public interface ServiceProducerTemplate {

    Message callService(Service service, Message message, Message parentMessage);
    public Message callService(Service service, Message message);

    public Message callService(String serviceCode, JsonNode payload);

    <T> T callService(String serviceCode, @Nullable Object request, Class<T> responseType);

    public Message callServiceWithException(String serviceCode, JsonNode payload);

    public Message callService(String serviceCode);

}
