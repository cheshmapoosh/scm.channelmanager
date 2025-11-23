package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.service.EbServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.EbServiceFilterRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;

public interface ScmServiceService {

    PagedResponseData<EbService> findServiceList(EbServiceFilterRequest request);
    EbService createService(EbServiceCreateRequest request);
    EbService findByServiceId(Short id);
    EbService updateService(EbServiceCreateRequest request);
}
