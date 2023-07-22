package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.service.DirectServiceImplementation;
import ir.daneshrefah.scm.common.model.service.JavaServiceImplementation;
import ir.daneshrefah.scm.common.model.service.ServiceRelation;
import ir.daneshrefah.scm.entity.service.ServiceEntity;
import ir.daneshrefah.scm.entity.service.ServiceRelationEntity;
import ir.daneshrefah.scm.mapper.ServiceMapper;
import ir.daneshrefah.scm.mapper.ServiceRelationMapper;
import ir.daneshrefah.scm.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ServiceRelationService {

    @Autowired
    ServiceRelationRepository serviceRelationRepository;

    public List<ServiceRelation> findServiceRelationListByServiceId(String serviceId) {
        List<ServiceRelationEntity> serviceRelationEntities = serviceRelationRepository
                .findServiceRelationEntityByServiceEntityId(serviceId);
        return ServiceRelationMapper.INSTANCE.entitiesToModels(serviceRelationEntities);
    }
}
