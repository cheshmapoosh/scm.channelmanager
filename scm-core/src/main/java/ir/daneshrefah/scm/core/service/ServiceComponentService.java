package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentEntity;
import ir.daneshrefah.scm.core.mapper.ServiceComponentMapper;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.core.repository.ServiceComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceComponentService {

    @Autowired
    ServiceComponentRepository serviceComponentRepository;

    public List<ServiceComponent> findListByServiceComponentProvider(String serviceComponentProviderId) {
        Iterable<ServiceComponentEntity> serviceComponentProviderEntities = serviceComponentRepository
                .findListByServiceComponentProviderEntityId(serviceComponentProviderId);
        return ServiceComponentMapper.INSTANCE.entitiesToModels(serviceComponentProviderEntities);
    }

    public ServiceComponent findServiceComponentByCodeAndProviderCode(String serviceComponentProviderCode,
                                                                      String serviceComponentCode) {
        ServiceComponentEntity serviceComponentProviderEntities = serviceComponentRepository
                .findByCodeAndProviderCode(serviceComponentCode, serviceComponentProviderCode);
        return ServiceComponentMapper.INSTANCE.toModel(serviceComponentProviderEntities);
    }

}
