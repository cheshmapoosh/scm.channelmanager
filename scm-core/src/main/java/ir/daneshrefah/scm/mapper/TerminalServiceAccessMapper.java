package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.service.ServiceRelation;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.entity.service.ServiceRelationEntity;
import ir.daneshrefah.scm.entity.terminal.TerminalServiceAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TerminalServiceAccessMapper {

    TerminalServiceAccessMapper INSTANCE = Mappers.getMapper(TerminalServiceAccessMapper.class);

    @Mapping(source = "serviceEntity", target = "service")
//    @Mapping(source = "terminalEntity", target = "terminal")
    TerminalServiceAccess toModel(TerminalServiceAccessEntity entity);

    @Mapping(source = "serviceEntity", target = "service")
//    @Mapping(source = "terminalEntity", target = "terminal")
    List<TerminalServiceAccess> entitiesToModels(List<TerminalServiceAccessEntity> entities);

}
