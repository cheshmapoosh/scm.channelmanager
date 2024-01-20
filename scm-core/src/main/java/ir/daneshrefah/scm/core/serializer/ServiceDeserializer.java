package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;

import java.io.IOException;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_BODY_IS_INVALID;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
public class ServiceDeserializer extends JsonDeserializer<Service> {

    public static final ServiceDeserializer INSTANT = new ServiceDeserializer();

    @Override
    public Service deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (null == node || node.isNull())
            return null;
        ServiceImplementationType implementationType = null;
        if (node.has("implementationType") && node.get("implementationType").isNumber())
            implementationType = ServiceImplementationType.findByCode(node.get("implementationType").asInt());
        else if (node.has("implementationType") && node.get("implementationType").isTextual())
            implementationType = ServiceImplementationType.valueOf(node.get("implementationType").asText());
        if (null == implementationType)
            return null;
        ir.daneshrefah.scm.common.model.service.Service newService = null;
        try {
            switch (implementationType) {
                case EXTERNAL:
                    newService = jsonParser.getCodec().treeToValue(node, ExternalService.class);
                    break;
                case JAVA:
                    newService = jsonParser.getCodec().treeToValue(node, JavaService.class);
                    break;
                case COMPOSITION:
                    newService = jsonParser.getCodec().treeToValue(node, CompositionService.class);
                    break;
                case PARENT:
                    newService = jsonParser.getCodec().treeToValue(node, ParentService.class);
                    break;
                case BPMN:
                    throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID, "service implementation type is invalid.");
                default:
                    throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID, "service implementation type is invalid.");
            }
        } catch (JsonProcessingException e) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_BODY_IS_INVALID, e.getMessage(), e);
        }

        return newService;
    }

}
