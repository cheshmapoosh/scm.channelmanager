package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.entity.ServiceComponentProviderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceComponentProviderMapper {
    ServiceComponentProviderMapper INSTANCE = Mappers.getMapper(ServiceComponentProviderMapper.class);

    ServiceComponentProvider toModel(ServiceComponentProviderEntity entity);

    List<ServiceComponentProvider> entitiesToModels(Iterable<ServiceComponentProviderEntity> entities);

}
