package ir.daneshrefah.scm.common.service.operation;

import ir.daneshrefah.scm.common.dto.operation.OperationFilterRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.operation.Operation;

import java.util.List;

public interface OperationService {
    List<Operation> getAllOperations();
    PagedResponseData<OperationResponse> getAllOperationsByFilter(OperationFilterRequest request);
    OperationResponse findById(String id);
}
