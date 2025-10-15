package ir.daneshrefah.scm.common.service.operationProvider;

import ir.daneshrefah.scm.common.dto.operationProvide.OperationProviderResponse;

import java.util.List;

public interface OperationProviderService {
    List<OperationProviderResponse> findAll();

    OperationProviderResponse findById(String id);
}