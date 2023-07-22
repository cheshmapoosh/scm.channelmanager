package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.component.ServiceComponent;
import ir.daneshrefah.scm.entity.component.ServiceComponentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceComponentMapper {
    ServiceComponentMapper INSTANCE = Mappers.getMapper(ServiceComponentMapper.class);

    ServiceComponent toModel(ServiceComponentEntity entity);

    List<ServiceComponent> entitiesToModels(Iterable<ServiceComponentEntity> entities);
}
