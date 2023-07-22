package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.component.ServiceComponent;
import ir.daneshrefah.scm.entity.ServiceComponentEntity;
import ir.daneshrefah.scm.entity.ServiceComponentProviderEntity;
import ir.daneshrefah.scm.mapper.ServiceComponentMapper;
import ir.daneshrefah.scm.mapper.ServiceComponentProviderMapper;
import ir.daneshrefah.scm.repository.ServiceComponentProviderRepository;
import ir.daneshrefah.scm.repository.ServiceComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceComponentService {

    @Autowired
    ServiceComponentRepository serviceComponentRepository;

    public List<ServiceComponent> findListByServiceComponentProvider(String serviceComponentProviderId) {
        Iterable<ServiceComponentEntity> serviceComponentProviderEntities = serviceComponentRepository
                .findListByServiceComponentProviderId(serviceComponentProviderId);
        return ServiceComponentMapper.INSTANCE.entitiesToModels(serviceComponentProviderEntities);
    }
}
