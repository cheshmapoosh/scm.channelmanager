package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceEditRequest;
import ir.daneshrefah.scm.common.model.service.Service;

public interface CompositionServicesService {

    Service getCompositionService(String compositionServiceId);

    Service editCompositionService(CompositionServiceEditRequest request);

    Service createCompositionService(CompositionServiceCreateRequest request);
}
