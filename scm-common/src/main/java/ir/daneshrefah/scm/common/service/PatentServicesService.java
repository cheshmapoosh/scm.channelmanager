package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.ParentServiceFindRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceEditRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.ScmService;

public interface PatentServicesService {
    ScmService createParentService(ParentServiceCreateRequest request);

    ScmService editParentService(ParentServiceEditRequest request);

    ScmService getParentService(String parentServiceId);

    PagedResponseData<ScmService> findParentServiceList(ParentServiceFindRequest request);
}
