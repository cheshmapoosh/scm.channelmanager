package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Service
public class DynamicTransformer extends AbstractJsonTransformer {

    private JsonNode emptyJsonNode;

    public DynamicTransformer() {
        ObjectMapper objectMapper = new ObjectMapper();
        emptyJsonNode = objectMapper.createObjectNode();
    }

    @Override
    public JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        return emptyJsonNode;
    }
}
