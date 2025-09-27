package ir.daneshrefah.scm.common.data.mapper.operation;

import ir.daneshrefah.scm.common.data.entity.operation.OperationEntity;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.model.operation.Operation;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OperationDefinitionMapper.class})
public interface OperationMapper {
    OperationEntity toEntity(Operation operation);

    @Mapping(target = "definitions", source = "definitions", qualifiedByName = "toModel")
    Operation toModel(OperationEntity operationEntity);

    @Mapping(target = "operationProviderTitle", source = "provider.title")
    @Mapping(target = "operationProviderName", source = "provider.name")
    OperationResponse toResponse(OperationEntity operationEntity);

}