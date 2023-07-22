package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.component.ServiceComponent;
import ir.daneshrefah.scm.common.model.service.DirectServiceImplementation;
import ir.daneshrefah.scm.common.model.service.JavaServiceImplementation;
import ir.daneshrefah.scm.common.model.service.ServiceImplementation;
import ir.daneshrefah.scm.common.model.service.ServiceRelation;
import ir.daneshrefah.scm.entity.component.ServiceComponentEntity;
import ir.daneshrefah.scm.entity.service.ServiceEntity;
import ir.daneshrefah.scm.mapper.ServiceComponentMapper;
import ir.daneshrefah.scm.mapper.ServiceMapper;
import ir.daneshrefah.scm.repository.ServiceComponentRepository;
import ir.daneshrefah.scm.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ServiceService {

    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ServiceRelationService serviceRelationService;

    public List<ir.daneshrefah.scm.common.model.service.Service> findServiceList() {
        Iterable<ServiceEntity> serviceList = serviceRepository.findAll();
        List<ir.daneshrefah.scm.common.model.service.Service> result = new ArrayList<>();
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
        return result;
    }
}
