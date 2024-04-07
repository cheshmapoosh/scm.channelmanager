package ir.daneshrefah.scm.plugin.api.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-07
 */
@RequiredArgsConstructor
public abstract class DynamicLookupTransformer extends AbstractTransformer {

    private final Map<String, AbstractTransformer> transformerMap;

    @Override
    protected final JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        String transformerKey = extractTransformerKey(message);
        AbstractTransformer transformer = transformerMap.get(transformerKey);
        return transformer.transform(payload, message, metadata);
    }

    protected abstract String extractTransformerKey(Message message);

//    protected abstract JsonNode doTransform(Object payload, Message message, JsonNode metadata);

}
