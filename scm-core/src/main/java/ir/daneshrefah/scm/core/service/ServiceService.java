package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.service.ServiceComponentRelationEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.mapper.ServiceComponentRelationMapper;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.plugin.api.model.service.DirectServiceImplementation;
import ir.daneshrefah.scm.core.repository.ServiceComponentRelationRepository;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceService.class);
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ServiceComponentRelationRepository serviceComponentRelationRepository;

    public List<ir.daneshrefah.scm.plugin.api.model.service.Service> findServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findAll();
//        List<ir.daneshrefah.scm.plugin.api.model.service.Service> services = ServiceMapper.INSTANCE.entitiesToModels(serviceEntities);
        List<ir.daneshrefah.scm.plugin.api.model.service.Service> services = new ArrayList<>();
        for (Iterator<ServiceEntity> iterator = serviceEntities.iterator(); iterator.hasNext(); ) {
            ServiceEntity serviceEntity = iterator.next();
            ir.daneshrefah.scm.plugin.api.model.service.Service service = ServiceMapper.INSTANCE.toModel(serviceEntity);
            switch (service.getImplementationType()) {
                case DIRECT:
                    DirectServiceImplementation directImplementation = new DirectServiceImplementation(service);
                    List<ServiceComponentRelationEntity> serviceRelations = serviceComponentRelationRepository.
                            findServiceRelationEntityByServiceEntityId(serviceEntity.getId());
                    directImplementation.setServiceComponentRelations(ServiceComponentRelationMapper.INSTANCE.entitiesToModels(serviceRelations));
                    service.setImplementation(directImplementation);
                    break;
                case JAVA:
                    LOGGER.warn("implementationType JAVA is defined for service '{}' but not implemented.", service.getCode());
                    break;
                case BPMN:
                    LOGGER.warn("implementationType BPMN is defined for service '{}' but not implemented.", service.getCode());
                    break;
                default:
                    LOGGER.error("invalid implementationType for service '{}'.", service.getCode());
                    break;
            }
            services.add(service);
        }
        return services;
/*        List<ir.daneshrefah.scm.common.model.service.Service> result = new ArrayList<>();
        for (Iterator<ServiceEntity> iterator = serviceList.iterator(); iterator.hasNext(); ) {
            ServiceEntity serviceEntity = iterator.next();
            ir.daneshrefah.scm.common.model.service.Service service = ServiceMapper.INSTANCE.toModel(serviceEntity);
            switch (serviceEntity.getType()) {
                case DIRECT:
                    DirectServiceImplementation directImplementation = new DirectServiceImplementation();
                    List<ServiceRelation> serviceRelations = serviceRelationService.findServiceRelationListByServiceId(serviceEntity.getId());
                    directImplementation.setServiceRelations(serviceRelations);
                    service.setImplementation(directImplementation);
                    break;
                case JAVA:
                    JavaServiceImplementation javaImplementation = new JavaServiceImplementation();
//                    implementation.setServiceClassName();
                    service.setImplementation(javaImplementation);
                    break;
                case BPMN:
                    System.out.println("It's Wednesday.");
                    break;
                default:
                    System.out.println("Invalid day of the week.");
                    break;
            }
            result.add(service);
        }
        return result;*/
    }
}
