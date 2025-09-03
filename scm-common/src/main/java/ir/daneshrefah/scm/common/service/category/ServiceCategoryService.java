package ir.daneshrefah.scm.common.service.category;

import ir.daneshrefah.scm.common.dto.service.ServiceCategoryRequest;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;

public interface ServiceCategoryService {

    ServiceCategoryResponse getServiceCategoryById(Short id);

    PagedResponseData<ServiceCategoryResponse> getAllServiceCategory(ServiceCategoryRequest request);
}
