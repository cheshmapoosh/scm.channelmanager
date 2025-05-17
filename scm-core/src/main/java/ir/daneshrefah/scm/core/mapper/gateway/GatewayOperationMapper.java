package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ServiceMapper.class})
public interface GatewayOperationMapper {
    ServiceOperationEntity toEntity(ServiceOperation serviceOperation);

    ServiceOperation toDto(ServiceOperationEntity serviceOperationEntity);

}