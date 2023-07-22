package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.entity.service.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    Service toModel(ServiceEntity entity);

//    List<ServiceComponent> entitiesToModels(Iterable<ServiceComponentEntity> entities);
}
