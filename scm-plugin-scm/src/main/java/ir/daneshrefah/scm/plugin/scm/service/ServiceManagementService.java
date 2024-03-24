package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidRequestFormatException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.service.ServiceFindRequest;
import ir.daneshrefah.scm.common.service.ServiceInfoRequest;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

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

    public PagedResponseData<ir.daneshrefah.scm.common.model.service.Service> serviceList(ServiceFindRequest request) {
        return this.service.findServiceList(request);
    }

    public ir.daneshrefah.scm.common.model.service.Service findServiceByCode(String serviceCode) {
//        String serviceCode = message.getPayloadValue("serviceCode");
        if (StringUtils.isEmpty(serviceCode)) {
            return null;
        }
        return this.service.findServiceByCode(serviceCode);
    }

    public ir.daneshrefah.scm.common.model.service.Service updateService(Message message) {
        if (null == message.getPayload() || message.getPayload().isNull() || message.getPayload().isEmpty()) {
            throw new MissingRequiredInputException("service data");
        }
        String serviceId = message.getPayloadValue("id");
        if (StringUtils.isEmpty(serviceId)) {
            throw new MissingRequiredInputException("serviceId");
        }
        ir.daneshrefah.scm.common.model.service.Service newService = null;
        try {
            newService = objectMapper.treeToValue(message.getPayload(),
                    ir.daneshrefah.scm.common.model.service.Service.class);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestFormatException("payload", e);
        }
        return this.service.updateService(serviceId, newService);
    }

    public ir.daneshrefah.scm.common.model.service.Service createService(ServiceInfoRequest request) {
        return this.service.createService(request);
    }

}
