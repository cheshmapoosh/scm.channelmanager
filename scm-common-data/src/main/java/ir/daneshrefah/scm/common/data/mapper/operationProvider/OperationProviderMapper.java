package ir.daneshrefah.scm.common.data.mapper.operationProvider;

import ir.daneshrefah.scm.common.data.entity.operation.OperationProviderEntity;
import ir.daneshrefah.scm.common.dto.operationProvide.OperationProviderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OperationProviderMapper {

    OperationProviderEntity toEntity(OperationProviderResponse response);

    OperationProviderResponse toModel(OperationProviderEntity entity);
}
