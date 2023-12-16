package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.repository.entity.ClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    Client toModel(ClientEntity entity);

    List<Client> toModels(Iterable<ClientEntity> clientEntities);

}
