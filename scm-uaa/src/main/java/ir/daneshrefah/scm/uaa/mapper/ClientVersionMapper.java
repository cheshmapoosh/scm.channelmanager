package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ClientVersionMapper {
    ClientVersionMapper INSTANCE= Mappers.getMapper(ClientVersionMapper.class);

    @Mapping(target = "clientId",expression = "java(getClientId(entity))")
    ClientVersion toModel(ClientVersionEntity entity);
    ClientVersionEntity toEntity(ClientVersion model);

    default Long getClientId(ClientVersionEntity entity){
        if (Objects.nonNull(entity) && Objects.nonNull(entity.getClient())){
            return entity.getClient().getId();
        }
        return null;
    }
}
