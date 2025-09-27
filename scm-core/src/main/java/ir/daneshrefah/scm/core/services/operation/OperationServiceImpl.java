package ir.daneshrefah.scm.core.services.operation;

import ir.daneshrefah.scm.common.data.entity.operation.OperationEntity;
import ir.daneshrefah.scm.common.data.mapper.operation.OperationMapper;
import ir.daneshrefah.scm.common.data.repository.operation.OperationRepository;
import ir.daneshrefah.scm.common.data.repository.operation.OperationSpecification;
import ir.daneshrefah.scm.common.dto.operation.OperationFilterRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.log.utils.PageableUtils;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return operationEntities.stream().map(operationMapper::toModel).toList();
    }

    @Override
    public OperationResponse findById(String id) {
        OperationEntity operationEntity = operationRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("processID"));
        return operationMapper.toResponse(operationEntity);
    }

    @Override
    public PagedResponseData<OperationResponse> getAllOperationsByFilter(OperationFilterRequest request) {
        Specification<OperationEntity> specification = OperationSpecification.toSpecification(request);
        Pageable pageable = PageableUtils.getPageable(request);
        Page<OperationEntity> operationEntityPage = operationRepository.findAll(specification, pageable);
        List<OperationResponse> operationList = operationEntityPage.getContent().stream().map(operationMapper::toResponse).toList();
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), operationEntityPage.getTotalElements(), operationList);
    }

}
