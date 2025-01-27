package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.ParentServiceFindRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceEditRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.Service;

public interface PatentServicesService {
    Service createParentService(ParentServiceCreateRequest request);

    Service editParentService(ParentServiceEditRequest request);

    Service getParentService(String parentServiceId);

    PagedResponseData<Service> findParentServiceList(ParentServiceFindRequest request);
}
