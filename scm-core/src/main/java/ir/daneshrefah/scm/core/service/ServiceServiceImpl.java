package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

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

    @Override
    public ir.daneshrefah.scm.common.model.service.Service createService(ir.daneshrefah.scm.common.model.service.Service service) {
        if (StringUtils.isEmpty(service.getCode())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_EMPTY, "service code is empty.");
        }
        if (StringUtils.isEmpty(service.getTitle())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_TITLE_IS_EMPTY, "service title is empty.");
        }
        if (null == service.getType()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_TYPE_IS_EMPTY, "service type is empty.");
        }
        if (ServiceImplementationType.JAVA.equals(service.getImplementationType()) &&
                StringUtils.isEmpty(((JavaService) service).getJavaImplementationClassName())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_JAVA_CLASS_IS_EMPTY, "service java class name is empty.");
        }
        if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType()) &&
                null == ((ExternalService) service).getServiceProvider()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_EMPTY, "service provider is empty.");
        }
        if (null == service.getVersion()) {
            service.setVersion(1);
        }
        service.setIsSystemic(false);
        if (null == service.getStatus()) {
            service.setStatus(ServiceStatus.ACTIVE);
        }
        if (null == service.getCheckAccessFirstAuthentication())
            service.setCheckAccessFirstAuthentication(false);
        if (null == service.getCheckAccessSecondAuthentication())
            service.setCheckAccessSecondAuthentication(false);
        if (null == service.getCheckAccessService())
            service.setCheckAccessService(false);
        if (null == service.getCheckAccessAsset())
            service.setCheckAccessAsset(false);
        ServiceEntity entity = ServiceMapper.INSTANCE.toServiceEntity(service);
        return ServiceMapper.INSTANCE.toService(serviceRepository.save(entity));
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
