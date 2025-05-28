package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.data.mapper.ServiceMapper;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING, uses = {ServiceMapper.class})
public interface ServiceOperationMapper {
    ServiceOperationEntity toEntity(ServiceOperation serviceOperation);

    ServiceOperation toModel(ServiceOperationEntity serviceOperationEntity);

}