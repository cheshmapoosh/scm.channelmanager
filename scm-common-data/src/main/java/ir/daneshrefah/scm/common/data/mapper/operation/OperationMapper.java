package ir.daneshrefah.scm.common.data.mapper.operation;

import ir.daneshrefah.scm.common.data.entity.operation.OperationEntity;
import ir.daneshrefah.scm.common.dto.operation.OperationRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.model.operation.Operation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OperationDefinitionMapper.class})
public interface OperationMapper {
    OperationEntity toEntity(Operation operation);

    OperationEntity toEntity(OperationRequest operationRequest);

    @Mapping(target = "definitions", source = "definitions", qualifiedByName = "toModel")
    Operation toModel(OperationEntity operationEntity);

    @Mapping(target = "operationProviderTitle", source = "provider.title")
    @Mapping(target = "operationProviderName", source = "provider.name")
    OperationResponse toResponse(OperationEntity operationEntity);
}