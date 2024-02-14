package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

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
            if (null != s) {
                implementationType = s.getImplementationType();
            } else {
                throw new InvalidInputException("id");
            }
        }
        if (null == implementationType)
            throw new MissingRequiredInputException("implementationType");
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
                    throw new MethodNotSupportDataException("service implementation type is invalid.");
                default:
                    throw new MethodNotSupportDataException("service implementation type is invalid.");
            }
        } catch (JsonProcessingException e) {
            if (e.getCause() instanceof BaseException) {
                throw ((BaseException) e.getCause());
            }
            throw new InvalidRequestFormatException("payload", e);
        }

        if (null == newService.getImplementationType()) {
            newService.setImplementationType(implementationType);
        }
        return newService;
    }

}
