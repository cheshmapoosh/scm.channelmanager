package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceEditRequest;
import ir.daneshrefah.scm.common.model.service.ScmService;

public interface CompositionServicesService {

    ScmService getCompositionService(String compositionServiceId);

    ScmService editCompositionService(CompositionServiceEditRequest request);

    ScmService createCompositionService(CompositionServiceCreateRequest request);
}
