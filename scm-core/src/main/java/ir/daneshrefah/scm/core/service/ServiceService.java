package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
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
//    @Autowired
//    ServiceComponentRelationRepository serviceComponentRelationRepository;
//    @Autowired
//    ServiceProducerTemplate serviceComponentExecutor;

    public List<ir.daneshrefah.scm.plugin.api.model.service.Service> findServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findAll();
        List<ir.daneshrefah.scm.plugin.api.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
        return services;
    }
}
