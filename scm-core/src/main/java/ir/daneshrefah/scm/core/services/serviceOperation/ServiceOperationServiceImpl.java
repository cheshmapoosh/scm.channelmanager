package ir.daneshrefah.scm.core.services.serviceOperation;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.data.mapper.EbServiceMapper;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.dto.serviceOperation.ServiceOperationCreateRequest;
import ir.daneshrefah.scm.common.dto.serviceOperation.ServiceOperationResponse;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.service.ScmServiceService;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.serviceOperation.ServiceOperationService;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOperationServiceImpl implements ServiceOperationService {

    private final ServiceOperationRepository serviceOperationRepository;
    private final ScmServiceService scmServiceService;
    private final DefinitionService definitionService;
    private final OperationService operationService;
    private final ServiceOperationMapper serviceOperationMapper;
    private final DefinitionMapper definitionMapper;
    private final EbServiceMapper ebServiceMapper;

    @Override
    public List<ServiceOperationResponse> getAll(Short serviceId) {
        return serviceOperationRepository.findAllByService_Id(serviceId)
                .stream()
                .map(serviceOperationMapper::toServiceOperationResponse)
                .toList();
    }

    @Override
    public List<ServiceOperationResponse> save(ServiceOperationCreateRequest request) {
        if (request.getOperationIds() == null || request.getOperationIds().isEmpty()) {
            throw new MissingRequiredInputException("operationIds");
        }

        if (request.getRoutingStrategy().equals(RoutingStrategy.FIRST) && request.getOperationIds().size() > 1) {
            throw new IllegalArgumentException("First routing strategy can only have one operation");
        }

        List<ServiceOperationEntity> serviceOperationEntityList = new ArrayList<>();
        EbService ebService = scmServiceService.findByServiceId(request.getServiceId());
        DefinitionResponse definitionResponse = definitionService.getDefinitionById(request.getDefinitionId());
        ServiceEntity serviceEntity = ebServiceMapper.toEntity(ebService);
        DefinitionEntity definitionEntity = definitionMapper.toEntity(definitionResponse);
        List<OperationResponse> operationResponses = operationService.getAllOperationByIds(request.getOperationIds());
        for (OperationResponse or : operationResponses) {
            ServiceOperationEntity serviceOperationEntity = new ServiceOperationEntity();
            serviceOperationEntity.setOperationName(or.getName());
            serviceOperationEntity.setActive(request.getActive());
            serviceOperationEntity.setService(serviceEntity);
            serviceOperationEntity.setDefinition(definitionEntity);
            serviceOperationEntityList.add(serviceOperationEntity);
        }
        serviceOperationRepository.saveAll(serviceOperationEntityList);
        return serviceOperationEntityList
                .stream()
                .map(serviceOperationMapper::toServiceOperationResponse)
                .toList();
    }
}
