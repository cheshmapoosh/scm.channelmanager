package ir.daneshrefah.scm.core.services.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.data.entity.operation.OperationEntity;
import ir.daneshrefah.scm.common.data.entity.operation.OperationProviderEntity;
import ir.daneshrefah.scm.common.data.mapper.operation.OperationMapper;
import ir.daneshrefah.scm.common.data.mapper.operationProvider.OperationProviderMapper;
import ir.daneshrefah.scm.common.data.repository.operation.OperationRepository;
import ir.daneshrefah.scm.common.data.repository.operation.OperationSpecification;
import ir.daneshrefah.scm.common.dto.operation.OperationFilterRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.dto.operationProvide.OperationProviderResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.log.utils.PageableUtils;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.operationProvider.OperationProviderService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {
    private final OperationRepository operationRepository;
    private final DefinitionService definitionService;
    private final OperationMapper operationMapper;
    private final OperationProviderService operationProviderService;
    private final OperationProviderMapper operationProviderMapper;

    @Override
    public List<Operation> getAllOperations() {
        List<OperationEntity> operationEntities = operationRepository.findAllByActive(Boolean.TRUE);
        if (operationEntities.isEmpty()) {
            return List.of();
        }
        return operationEntities.stream().map(operationMapper::toModel).toList();
    }

    @Override
    public List<Operation> findActiveOperationsByNames(Collection<String> operationNames) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        if (operationNames != null) {
            operationNames.stream()
                    .map(StringUtils::trimToNull)
                    .filter(name -> name != null)
                    .forEach(names::add);
        }
        if (names.isEmpty()) {
            return List.of();
        }
        return operationRepository.findByNameInAndActiveTrue(names)
                .stream()
                .map(operationMapper::toModel)
                .toList();
    }

    @Override
    public OperationResponse findById(String id) {
        OperationEntity operationEntity = operationRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("processID"));
        return operationMapper.toResponse(operationEntity);
    }

    @Override
    @Transactional
    public OperationResponse create(OperationRequest request) throws JsonProcessingException {
        ValidationUtils.checkNull(request.getDefinition(), () -> new MissingRequiredInputException("definition"));
        OperationEntity operationEntity = operationMapper.toEntity(request);
        OperationProviderResponse operationProviderResponse = operationProviderService.findById(request.getOperationProviderId());
        String name = request.getName().toUpperCase();
        if (request.getType().equals(OperationType.JAVA) && !name.startsWith("SVC")) {
            name = "SVC_" + request.getName().toUpperCase();
        }
        operationEntity.setName(name);
        request.getDefinition().setName(name);
        request.getDefinition().setTitle(request.getTitle());
        OperationProviderEntity operationProviderEntity = operationProviderMapper.toEntity(operationProviderResponse);
        operationEntity.setProvider(operationProviderEntity);
        definitionService.createDefinition(request.getDefinition());
        operationRepository.save(operationEntity);
        return operationMapper.toResponse(operationEntity);
    }

    @Override
    public OperationResponse update(OperationRequest request) {
//        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("id"));
//        OperationEntity operationEntity = operationRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("operation"));
//        OperationProviderResponse operationProviderResponse = operationProvider.findById(request.getOperationProviderId());
//        if (request.getType().equals(OperationType.REST) && !operationProviderResponse.getName().startsWith("SVC")) {
//            operationProviderResponse.setName("SVC_" + operationProviderResponse.getName());
//        }
//        OperationProviderEntity operationProviderEntity = operationProviderMapper.toEntity(operationProviderResponse);
//        OperationEntity entity = operationMapper.toEntity(request);
//        entity.setVersion(operationEntity.getVersion());
//        entity.setProvider(operationProviderEntity);
//        operationRepository.save(entity);
//        return operationMapper.toResponse(entity);
        return null;
    }

    @Override
    public PagedResponseData<OperationResponse> getAllOperationsByFilter(OperationFilterRequest request) {
        Specification<OperationEntity> specification = OperationSpecification.toSpecification(request);
        Pageable pageable = PageableUtils.getPageable(request);
        Page<OperationEntity> operationEntityPage = operationRepository.findAll(specification, pageable);
        List<OperationResponse> operationList = operationEntityPage.getContent().stream().map(operationMapper::toResponse).toList();
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), operationEntityPage.getTotalElements(), operationList);
    }

    @Override
    public List<OperationResponse> getAllOperationByIds(List<String> ids) {
        return operationRepository.findAllById(ids).stream().map(operationMapper::toResponse).toList();
    }
}
