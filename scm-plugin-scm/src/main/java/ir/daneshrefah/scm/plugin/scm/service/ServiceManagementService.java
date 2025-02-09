package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.JavaMethodType;
import ir.daneshrefah.scm.common.constant.Status;
import ir.daneshrefah.scm.common.dto.*;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceEditRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceEditRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceEditRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.service.JavaServicesService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
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

    public ServiceManagementService(ServiceProducerTemplate producerTemplate,
                                    ObjectMapper objectMapper,
                                    ServiceService service) {
        super(producerTemplate, objectMapper);
        this.service = service;
    }


    @JavaService(serviceCode = SVC_SERVICE_LIST)
    public PagedResponseData<Service> serviceList(ServiceFindRequest request) {
        return this.service.findServiceList(request);
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

    // PARENT SERVICE

    @JavaService(serviceCode = SVC_SERVICE_CREATE_PARENT)
    public ParentService createParentService(ParentServiceCreateRequest request) {
        return (ParentService) this.service.createParentService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_EDIT_PARENT)
    public ParentService editParentService(ParentServiceEditRequest request) {
        return (ParentService) this.service.editParentService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_FIND_PARENT)
    public ParentService getParentService(String parentServiceId) {
        return (ParentService) this.service.getParentService(parentServiceId);
    }

    @JavaService(serviceCode = SVC_PARENT_SERVICE_LIST)
    public PagedResponseData<Service> parentServiceList(ParentServiceFindRequest request) {
        return this.service.findParentServiceList(request);
    }

    // JAVA SERVICE

    @JavaService(serviceCode = SVC_SERVICE_CREATE_JAVA)
    public ir.daneshrefah.scm.plugin.api.model.service.java.JavaService createJavaService(JavaServiceCreateRequest request) {
        return (ir.daneshrefah.scm.plugin.api.model.service.java.JavaService) this.service.createJavaService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_EDIT_JAVA)
    public ir.daneshrefah.scm.plugin.api.model.service.java.JavaService editJavaService(JavaServiceEditRequest request) {
        return (ir.daneshrefah.scm.plugin.api.model.service.java.JavaService) this.service.editJavaService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_FIND_JAVA)
    public ir.daneshrefah.scm.plugin.api.model.service.java.JavaService getJavaService(String javaServiceId) {
        return (ir.daneshrefah.scm.plugin.api.model.service.java.JavaService) this.service.getJavaService(javaServiceId);
    }

    // COMPOSITE SERVICE

    @JavaService(serviceCode = SVC_SERVICE_CREATE_COMPOSITION)
    public CompositionService createCompositionService(CompositionServiceCreateRequest request) {
        return (CompositionService) this.service.createCompositionService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_EDIT_COMPOSITION)
    public CompositionService editCompositionService(CompositionServiceEditRequest request) {
        return (CompositionService) this.service.editCompositionService(request);
    }

    @JavaService(serviceCode = SVC_SERVICE_FIND_COMPOSITION)
    public CompositionService getCompositionService(String compositionServiceId) {
        return (CompositionService) this.service.getCompositionService(compositionServiceId);
    }



}
