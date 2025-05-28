package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.gateway.AuthenticationMethodEntity;
import ir.daneshrefah.scm.common.model.gateway.AuthenticationMethod;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface AuthenticationMethodMapper {
    AuthenticationMethodEntity toEntity(AuthenticationMethod authenticationMethod);

    AuthenticationMethod toModel(AuthenticationMethodEntity authenticationMethodEntity);
}
