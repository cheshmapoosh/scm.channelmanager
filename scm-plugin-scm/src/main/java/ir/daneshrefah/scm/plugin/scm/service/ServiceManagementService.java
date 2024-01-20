package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@Service
public class ServiceManagementService extends AbstractJavaService {

    private final ServiceService service;

    public ServiceManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ServiceService service) {
        super(producerTemplate, objectMapper);
        this.service = service;
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> serviceList(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        return this.service.findServiceList();
    }

    public ir.daneshrefah.scm.common.model.service.Service findServiceByCode(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        String serviceCode = message.getPayloadValue("serviceCode");
        if (StringUtils.isEmpty(serviceCode)) {
            return null;
        }
        return this.service.findServiceByCode(serviceCode);
    }

    public ir.daneshrefah.scm.common.model.service.Service updateService(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        if (null == message.getPayload() || message.getPayload().isNull() || message.getPayload().isEmpty()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_BODY_IS_EMPTY, "service data is empty.");
        }
        String serviceId = message.getPayloadValue("serviceId");
        if (StringUtils.isEmpty(serviceId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_ID_IS_EMPTY, "service id is not specified.");
        }
        ir.daneshrefah.scm.common.model.service.Service newService = null;
        try {
            newService = objectMapper.treeToValue(message.getPayload(),
                    ir.daneshrefah.scm.common.model.service.Service.class);
        } catch (JsonProcessingException e) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_BODY_IS_INVALID, e.getMessage(), e);
        }
        return this.service.updateService(serviceId, newService);
    }

    public ir.daneshrefah.scm.common.model.service.Service createService(Message message, ir.daneshrefah.scm.common.model.service.Service service, Object payload) {
        if (null == message.getPayload() || message.getPayload().isNull() || message.getPayload().isEmpty()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_BODY_IS_EMPTY, "service data is empty.");
        }
        ServiceImplementationType implementationType = ServiceImplementationType.findByCode(message.getIntegerPayloadValue("implementationType"));
        if (null == implementationType) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_EMPTY, "service implementation type is not specified.");
        }
        ir.daneshrefah.scm.common.model.service.Service newService = null;
        try {
            switch (implementationType) {
                case EXTERNAL:
                    newService = objectMapper.treeToValue(message.getPayload(), ExternalService.class);
                    break;
                case JAVA:
                    newService = objectMapper.treeToValue(message.getPayload(), JavaService.class);
                    break;
                case COMPOSITION:
                    newService = objectMapper.treeToValue(message.getPayload(), CompositionService.class);
                    break;
                case PARENT:
                    newService = objectMapper.treeToValue(message.getPayload(), ParentService.class);
                    break;
                case BPMN:
                    throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID, "service implementation type is invalid.");
                default:
                    throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID, "service implementation type is invalid.");
            }
        } catch (JsonProcessingException e) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_BODY_IS_INVALID, e.getMessage(), e);
        }
        newService.setCreator(message.getHeader().getAuthentication().getPersonUsername());
        newService.setLastEditor(message.getHeader().getAuthentication().getPersonUsername());
        return this.service.createService(newService);
    }

}
