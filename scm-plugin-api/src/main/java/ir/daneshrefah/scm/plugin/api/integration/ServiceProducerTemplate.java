package ir.daneshrefah.scm.plugin.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ScmService;
import org.springframework.lang.Nullable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public interface ServiceProducerTemplate {

    Message callService(ScmService service, Message message, Message parentMessage);

    Message callService(ScmService service, Message message);

    Message callService(String serviceCode, JsonNode payload);

    <T> T callService(String serviceCode, @Nullable Object request, Class<T> responseType);
    <T> T callServiceWithException(String serviceCode, @Nullable Object request, Class<T> responseType);

    Message callServiceWithException(String serviceCode, JsonNode payload);

    Message callService(String serviceCode);
    Message callService(String serviceCode, Message message);

}
