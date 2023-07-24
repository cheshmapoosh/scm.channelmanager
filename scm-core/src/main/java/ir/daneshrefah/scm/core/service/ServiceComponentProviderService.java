package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentProviderEntity;
import ir.daneshrefah.scm.core.mapper.ServiceComponentProviderMapper;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.core.repository.ServiceComponentProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceComponentProviderService {

    @Autowired
    ServiceComponentProviderRepository serviceComponentProviderRepository;

    public List<ServiceComponentProvider> findAllServiceComponentProviders() {
        Iterable<ServiceComponentProviderEntity> serviceComponentProviderEntities = serviceComponentProviderRepository.findAll();
        return ServiceComponentProviderMapper.INSTANCE.entitiesToModels(serviceComponentProviderEntities);
    }

}
