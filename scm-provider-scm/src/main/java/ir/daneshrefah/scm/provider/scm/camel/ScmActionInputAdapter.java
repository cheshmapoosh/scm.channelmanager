package ir.daneshrefah.scm.provider.scm.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.scm.exception.ScmResourceProviderException;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceActionDescriptor;
import org.springframework.util.ClassUtils;

final class ScmActionInputAdapter {

    private final ObjectMapper objectMapper;

    ScmActionInputAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Object adapt(ScmResourceActionDescriptor action, Object body) {
        if (!action.acceptsInput()) {
            return body;
        }

        Class<?> inputType = action.inputType();
        if (body instanceof ir.daneshrefah.scm.common.model.message.Message message) {
            body = message.getPayload();
        }
        if (body instanceof JsonNode jsonNode && jsonNode.isNull()) {
            body = null;
        }
        if (body == null) {
            if (inputType.isPrimitive()) {
                throw ScmResourceProviderException.invalidActionInput();
            }
            return null;
        }

        Class<?> assignableInputType = ClassUtils.resolvePrimitiveIfNecessary(inputType);
        if (assignableInputType.isInstance(body)) {
            return body;
        }

        try {
            if (body instanceof String text) {
                if (text.isBlank()) {
                    throw ScmResourceProviderException.invalidActionInput();
                }
                return objectMapper.readValue(text, inputType);
            }
            return objectMapper.convertValue(body, inputType);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            throw ScmResourceProviderException.invalidActionInput();
        }
    }
}
