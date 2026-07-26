package ir.daneshrefah.scm.core.services.serviceOperation;

import ir.daneshrefah.scm.common.dto.serviceOperation.ServiceOperationResponse;
import ir.daneshrefah.scm.common.service.serviceOperation.ServiceOperationService;
import ir.daneshrefah.scm.core.mapper.gateway.ServiceOperationMapper;
import ir.daneshrefah.scm.core.repository.gateway.ServiceOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOperationServiceImpl implements ServiceOperationService {

    private final ServiceOperationRepository serviceOperationRepository;
    private final ServiceOperationMapper serviceOperationMapper;

    @Override
    public List<ServiceOperationResponse> getAll(Short serviceId) {
        return serviceOperationRepository.findAllByService_Id(serviceId)
                .stream()
                .map(serviceOperationMapper::toServiceOperationResponse)
                .toList();
    }
}
