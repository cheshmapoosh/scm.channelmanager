package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@Component
public class ServiceManagementService extends AbstractJavaService {

    private final ServiceService service;

    public ServiceManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ServiceService service) {
        super(producerTemplate, objectMapper);
        this.service = service;
    }

    // PARENT SERVICES


    @JavaService(serviceCode = SVC_SERVICE_LIST)
    public PagedResponseData<Service> serviceList(ServiceFindRequest request) {
        return this.service.findServiceList(request);
    }

    @JavaService(serviceCode = SVC_PARENT_SERVICE_LIST)
    public PagedResponseData<Service> parentServiceList(ParentServiceFindRequest request) {
        return this.service.findParentServiceList(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_BY_CODE)
    public Service findServiceByCode(String serviceCode) {
//        String serviceCode = message.getPayloadValue("serviceCode");
        if (StringUtils.isEmpty(serviceCode)) {
            return null;
        }
        return this.service.findServiceByCode(serviceCode);
    }

    @JavaService(serviceCode = SVC_SERVICE_EDIT)
    public Service updateService(ServiceInfoEditRequest request) {
        return this.service.updateService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_CREATE)
    public Service createService(ServiceInfoRequest request) {
        return this.service.createService(request);
    }

    @JavaService(serviceCode = SCV_SERVICE_DELETE)
    public void deleteService(ServiceDeleteRequest request) {
        this.service.deleteService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_ACCESS_SERVICE_LIST)
    public PagedResponseData<TerminalServiceAccessAssignmentResponse> findAllServiceAccessOnTerminal(ServiceAccessFindRequest request) {
        return this.service.findAllServiceAccessOnTerminal(request);
    }

}
