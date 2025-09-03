package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryRequest;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.service.category.ServiceCategoryService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Component;

@Component
public class ServiceCategoryServiceManagement extends AbstractJavaService {

    private final ServiceCategoryService serviceCategoryService;

    public ServiceCategoryServiceManagement(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ServiceCategoryService serviceCategoryService) {
        super(producerTemplate, objectMapper);
        this.serviceCategoryService = serviceCategoryService;
    }

    @JavaService(operationCode = OperationCode.SVC_SERVICE_CATEGORY_LIST)
    public PagedResponseData<ServiceCategoryResponse> getServiceCategoryService(ServiceCategoryRequest request) {
        return serviceCategoryService.getAllServiceCategory(request);
    }
}
