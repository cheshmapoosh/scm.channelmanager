package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClientVersionMapper {
    ClientVersionMapper INSTANCE= Mappers.getMapper(ClientVersionMapper.class);

    ClientVersion toModel(ClientVersionEntity clientVersionEntity);
    ClientVersionEntity toEntity(ClientVersion clientVersion);
}
