package ir.daneshrefah.scm.provider.scm.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.scm.exception.ScmResourceProviderException;
import ir.daneshrefah.scm.provider.scm.registry.ScmResourceActionDescriptor;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Array;
import java.util.Map;

final class ScmActionInputAdapter {

    private final ObjectMapper objectMapper;

    ScmActionInputAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Object adapt(ScmResourceActionDescriptor action, Object body) {
        if (!action.acceptsInput()) {
            return body;
        }

        JavaType inputType = action.inputType();
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

        if (isCompatible(inputType, body)) {
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

    private boolean isCompatible(JavaType expectedType, Object value) {
        if (value == null) {
            return !expectedType.isPrimitive();
        }

        Class<?> rawType = ClassUtils.resolvePrimitiveIfNecessary(expectedType.getRawClass());
        if (!rawType.isInstance(value)) {
            return false;
        }
        if (!expectedType.hasGenericTypes()) {
            return true;
        }
        if (expectedType.isMapLikeType() && value instanceof Map<?, ?> map) {
            JavaType keyType = expectedType.getKeyType();
            JavaType valueType = expectedType.getContentType();
            return map.entrySet().stream().allMatch(entry ->
                    isCompatible(keyType, entry.getKey())
                            && isCompatible(valueType, entry.getValue()));
        }
        if (expectedType.isCollectionLikeType() && value instanceof Iterable<?> iterable) {
            JavaType elementType = expectedType.getContentType();
            for (Object element : iterable) {
                if (!isCompatible(elementType, element)) {
                    return false;
                }
            }
            return true;
        }
        if (expectedType.isArrayType() && value.getClass().isArray()) {
            JavaType elementType = expectedType.getContentType();
            for (int index = 0; index < Array.getLength(value); index++) {
                if (!isCompatible(elementType, Array.get(value, index))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
