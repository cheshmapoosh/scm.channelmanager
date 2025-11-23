package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceCategoryEntity;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryResponse;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ServiceCategoryMapper {
    ServiceCategoryResponse toDto(ServiceCategoryEntity entity);

    ServiceCategoryEntity toEntity(ServiceCategoryResponse model);

    List<ServiceCategoryResponse> toDtos(List<ServiceCategoryEntity> entities);
}
