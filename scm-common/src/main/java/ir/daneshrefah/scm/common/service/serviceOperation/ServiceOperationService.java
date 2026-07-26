package ir.daneshrefah.scm.common.service.serviceOperation;

import ir.daneshrefah.scm.common.dto.serviceOperation.ServiceOperationResponse;

import java.util.List;

public interface ServiceOperationService {
    List<ServiceOperationResponse> getAll(Short serviceId);
}
