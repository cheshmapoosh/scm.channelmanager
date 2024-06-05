package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.bundle.ResourceBundleEntity;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ResourceBundleMapper {
    ResourceBundleMapper INSTANCE = Mappers.getMapper(ResourceBundleMapper.class);
    ResourceBundle toModel(ResourceBundleEntity entity);
    ResourceBundleEntity toEntity(ResourceBundle model);
}
