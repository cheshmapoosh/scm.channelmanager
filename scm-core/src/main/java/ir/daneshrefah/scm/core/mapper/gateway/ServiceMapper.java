package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.entity.gateway.ServiceEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    ServiceEntity toEntity(Service service);

    Service toDto(ServiceEntity serviceEntity);
}