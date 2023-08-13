package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Service
public class EmptyTransformer extends AbstractTransformer {

    private JsonNode emptyJsonNode;

    public EmptyTransformer() {
        ObjectMapper objectMapper = new ObjectMapper();
        emptyJsonNode = objectMapper.createObjectNode();
    }

    @Override
    public Object internalTransform(Object payload, Message message, String metadata) {
        return emptyJsonNode;
    }
}
