package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.gateway.AuthenticationMethodEntity;
import ir.daneshrefah.scm.common.model.gateway.AuthenticationMethod;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationMethodMapper {
    AuthenticationMethodEntity toEntity(AuthenticationMethod authenticationMethod);

    AuthenticationMethod toDto(AuthenticationMethodEntity authenticationMethodEntity);
}
