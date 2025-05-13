package ir.daneshrefah.scm.core.services.operation;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.core.entity.operation.OperationEntity;
import ir.daneshrefah.scm.core.mapper.operation.OperationMapper;
import ir.daneshrefah.scm.core.repository.operation.OperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {
    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;

    @Override
    public List<Operation> getAllOperations() {
        List<OperationEntity> operationEntities = operationRepository.findAllByActive(Boolean.TRUE);
        if (operationEntities.isEmpty()) {
            return List.of();
        }
        return operationEntities.stream().map(operationMapper::toDto).toList();
    }
}
