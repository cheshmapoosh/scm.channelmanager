package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.model.gateway.AuthenticationMethod;
import ir.daneshrefah.scm.core.entity.gateway.AuthenticationMethodEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationMethodMapper {
    AuthenticationMethodEntity toEntity(AuthenticationMethod authenticationMethod);

    AuthenticationMethod toDto(AuthenticationMethodEntity authenticationMethodEntity);
}