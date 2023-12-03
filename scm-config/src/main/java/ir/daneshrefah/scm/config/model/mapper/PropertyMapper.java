package ir.daneshrefah.scm.config.model.mapper;

import ir.daneshrefah.scm.config.model.property.Property;
import ir.daneshrefah.scm.config.model.property.PropertyDTO;
import ir.daneshrefah.scm.config.model.property.PropertyEditDTO;
import ir.daneshrefah.scm.config.model.property.EnvironmentProperty;
import ir.daneshrefah.scm.config.model.entity.PropertyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface PropertyMapper {

    PropertyMapper INSTANCE = Mappers.getMapper(PropertyMapper.class);

    List<PropertyDTO> toPropertiesDTO(List<PropertyEntity> propertyEntities);

    PropertyDTO toPropertyDTO(Optional<PropertyEntity> entity);
    List<EnvironmentProperty> toPropertiesModel(List<PropertyEntity> propertyEntities);

    @Mapping(target = "id", ignore = true)
    PropertyEntity toPropertyEntity(Property property);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    PropertyEntity toEditPropertyEntity(PropertyEditDTO propertyEditDTO);
}
