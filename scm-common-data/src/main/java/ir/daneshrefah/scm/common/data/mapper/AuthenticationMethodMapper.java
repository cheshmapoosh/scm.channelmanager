package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.gateway.AuthenticationMethodEntity;
import ir.daneshrefah.scm.common.model.gateway.AuthenticationMethod;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthenticationMethodMapper {
    AuthenticationMethodMapper INSTANCE = Mappers.getMapper(AuthenticationMethodMapper.class);
    AuthenticationMethod toModel(AuthenticationMethodEntity entity);
    AuthenticationMethodEntity toEntity(AuthenticationMethod method);
}
