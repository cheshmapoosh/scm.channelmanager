package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentProviderEntity;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceComponentProviderMapper {
    ServiceComponentProviderMapper INSTANCE = Mappers.getMapper(ServiceComponentProviderMapper.class);

    ServiceComponentProvider toModel(ServiceComponentProviderEntity entity);

    List<ServiceComponentProvider> entitiesToModels(Iterable<ServiceComponentProviderEntity> entities);

}
