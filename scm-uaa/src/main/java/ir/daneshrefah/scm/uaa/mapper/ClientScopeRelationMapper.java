package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientScopeRelationEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ScopeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface ClientScopeRelationMapper {
    ClientScopeRelationMapper INSTANCE = Mappers.getMapper(ClientScopeRelationMapper.class);

    @Mapping(target = "scope" ,source = "scope" , qualifiedByName = "toScopeEntity")
    @Mapping(target = "client" ,source = "client" , qualifiedByName = "toClientEntity")
    ClientScopeRelationEntity toEntity(ClientScopeRelation model);

    @Mapping(target = "scope" ,source = "scope" , qualifiedByName = "toScopeModel")
    @Mapping(target = "client" ,source = "client" , qualifiedByName = "toClientModel")
    ClientScopeRelation toModel(ClientScopeRelationEntity entity);

    List<ClientScopeRelationEntity> toScopeEntities(Iterable<ClientScopeRelation> entities);

    @Named("toClientModel")
    default Client toClientModel(ClientEntity entity) {
        if (Objects.nonNull(entity)) {
            return ClientMapper.INSTANCE.toModel(entity);
        }
        return null;
    }

    @Named("toClientEntity")
    default ClientEntity toClientModel(Client model) {
        if (Objects.nonNull(model)) {
            return ClientMapper.INSTANCE.toClientIdEntity(model);
        }
        return null;
    }

    @Named("toScopeModel")
    default Scope toClientModel(ScopeEntity entity) {
        if (Objects.nonNull(entity)) {
            return ScopeMapper.INSTANCE.toModel(entity);
        }
        return null;
    }

    @Named("toScopeEntity")
    default ScopeEntity toClientEntity(Scope model) {
        if (Objects.nonNull(model)) {
            return ScopeMapper.INSTANCE.toEntity(model);
        }
        return null;
    }
}
