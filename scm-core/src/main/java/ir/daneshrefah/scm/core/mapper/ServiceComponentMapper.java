package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentEntity;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceComponentMapper {
    ServiceComponentMapper INSTANCE = Mappers.getMapper(ServiceComponentMapper.class);

    @Mapping(source = "serviceComponentProviderEntity", target = "serviceComponentProvider")
    ServiceComponent toModel(ServiceComponentEntity entity);

    List<ServiceComponent> entitiesToModels(Iterable<ServiceComponentEntity> entities);
}
