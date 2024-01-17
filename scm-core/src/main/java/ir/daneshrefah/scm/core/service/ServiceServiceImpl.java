package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceServiceImpl implements ServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ServiceRelationRepository serviceRelationRepository;

    public List<ir.daneshrefah.scm.common.model.service.Service> findServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findAll();
        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
        return services;
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service findServiceByCode(String code) {
        Optional<ServiceEntity> entity = serviceRepository.findByCode(code);
        if (entity.isEmpty())
            return null;
        return ServiceMapper.INSTANCE.toService(entity.get());
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> findCallableServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findCallableServiceList();
        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
        return services;
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> findParentServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findServiceListByImplementationType(ServiceImplementationType.PARENT);
        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
        return services;
    }

    public List<ServiceRelation> findServiceRelationListBySourceServiceId(String sourceServiceId, ServiceRelationType relationType) {
        Iterable<ServiceRelationEntity> relationEntities = serviceRelationRepository.findAllBySourceServiceIdAndRelationType(sourceServiceId, relationType);
        List<ServiceRelation> relations = ServiceMapper.INSTANCE.relationEntitiesToModels(relationEntities);
        return relations;
    }

}
