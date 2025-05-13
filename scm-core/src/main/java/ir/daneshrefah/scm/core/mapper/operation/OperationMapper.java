package ir.daneshrefah.scm.core.mapper.operation;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.core.entity.operation.OperationEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OperationMapper {
    OperationEntity toEntity(Operation operation);

    Operation toDto(OperationEntity operationEntity);
}