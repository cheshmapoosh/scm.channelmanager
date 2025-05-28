package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.bundle.ResourceBundleEntity;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResourceBundleMapper {
    ResourceBundle toModel(ResourceBundleEntity entity);

    ResourceBundleEntity toEntity(ResourceBundle model);
}
