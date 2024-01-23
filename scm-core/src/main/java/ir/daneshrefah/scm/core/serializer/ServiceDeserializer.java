package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class ServiceDeserializer extends JsonDeserializer<Service> {

    private final ServiceService service;

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
        if (null == implementationType && node.has("id") && !node.get("id").isNull()) {
            Service s = service.findServiceById(node.get("id").asText());
            if (null != s)
                implementationType = s.getImplementationType();
        }
        if (null == implementationType)
            throw new ValidationException(null, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_IMPLEMENTATION_TYPE_IS_EMPTY,
                    "service 'implementationType' must be set.");
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
            if (e.getCause() instanceof BaseException) {
                throw ((BaseException) e.getCause());
            }
            throw new ValidationException(null, ERROR_CODE_VALIDATION_BODY_IS_INVALID, e.getMessage(), e);
        }

        if (null == newService.getImplementationType()) {
            newService.setImplementationType(implementationType);
        }
        return newService;
    }

}
