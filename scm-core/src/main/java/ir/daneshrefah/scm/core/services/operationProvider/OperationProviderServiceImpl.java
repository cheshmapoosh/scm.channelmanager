package ir.daneshrefah.scm.core.services.operationProvider;

import ir.daneshrefah.scm.common.data.entity.operation.OperationProviderEntity;
import ir.daneshrefah.scm.common.data.mapper.operationProvider.OperationProviderMapper;
import ir.daneshrefah.scm.common.data.repository.operationProvider.OperationProviderRepository;
import ir.daneshrefah.scm.common.dto.operationProvide.OperationProviderResponse;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.service.operationProvider.OperationProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationProviderServiceImpl implements OperationProviderService {
    private final OperationProviderRepository operationProviderRepository;
    private final OperationProviderMapper operationProviderMapper;

    public List<OperationProviderResponse> findAll() {
        List<OperationProviderEntity> operationProviderEntities = operationProviderRepository.findAll();
        return operationProviderEntities.stream().map(operationProviderMapper::toModel).toList();
    }

    @Override
    public OperationProviderResponse findById(String id) {
        OperationProviderEntity operationProviderEntity = operationProviderRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("OperationProvider id"));
        return operationProviderMapper
                .toModel(operationProviderEntity);
    }
}
