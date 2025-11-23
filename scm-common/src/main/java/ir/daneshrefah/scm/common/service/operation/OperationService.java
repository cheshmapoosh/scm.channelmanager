package ir.daneshrefah.scm.common.service.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.dto.operation.OperationFilterRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.operation.Operation;

import java.util.List;

public interface OperationService {
    OperationResponse findById(String id);

    List<Operation> getAllOperations();

    PagedResponseData<OperationResponse> getAllOperationsByFilter(OperationFilterRequest request);

    OperationResponse create(OperationRequest request) throws JsonProcessingException;

    OperationResponse update(OperationRequest request);

    List<OperationResponse> getAllOperationByIds(List<String> ids);
}
