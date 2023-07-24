package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.service.ServiceComponentRelationEntity;
import ir.daneshrefah.scm.plugin.api.model.service.ServiceComponentRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceComponentRelationMapper {

    ServiceComponentRelationMapper INSTANCE = Mappers.getMapper(ServiceComponentRelationMapper.class);

    @Mapping(source = "serviceComponentEntity", target = "serviceComponent")
    ServiceComponentRelation toModel(ServiceComponentRelationEntity entity);

    List<ServiceComponentRelation> entitiesToModels(List<ServiceComponentRelationEntity> entities);

}
