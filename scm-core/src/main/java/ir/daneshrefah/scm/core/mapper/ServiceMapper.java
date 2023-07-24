package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    Service toModel(ServiceEntity entity);

    List<Service> entitiesToModels(Iterable<ServiceEntity> entities);

}
