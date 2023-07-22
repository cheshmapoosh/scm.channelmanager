package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.service.ServiceRelation;
import ir.daneshrefah.scm.entity.service.ServiceRelationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ServiceRelationMapper {

    ServiceRelationMapper INSTANCE = Mappers.getMapper(ServiceRelationMapper.class);

    @Mapping(source = "serviceComponentEntity", target = "serviceComponent")
    ServiceRelation toModel(ServiceRelationEntity entity);

    @Mapping(source = "serviceComponentEntity", target = "serviceComponent")
    List<ServiceRelation> entitiesToModels(List<ServiceRelationEntity> entities);

}
