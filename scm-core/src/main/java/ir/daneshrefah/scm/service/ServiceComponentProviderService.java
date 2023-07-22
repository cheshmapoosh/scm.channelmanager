package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.entity.component.ServiceComponentProviderEntity;
import ir.daneshrefah.scm.mapper.ServiceComponentProviderMapper;
import ir.daneshrefah.scm.repository.ServiceComponentProviderRepository;
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
