package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.client.ClientScopeRelation;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientScopeRelationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy =IGNORE, componentModel = SPRING,uses = {ClientMapper.class,ScopeMapper.class})
public interface ClientScopeRelationMapper {

    @Mapping(source = "client",target = "client" , qualifiedByName = "toClientEntity")
    ClientScopeRelationEntity toEntity(ClientScopeRelation model);

    ClientScopeRelation toModel(ClientScopeRelationEntity entity);

    List<ClientScopeRelationEntity> toScopeEntities(Iterable<ClientScopeRelation> entities);

}
