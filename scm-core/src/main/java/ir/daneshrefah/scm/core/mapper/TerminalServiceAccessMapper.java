package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING,
        uses = {ScmServiceMapper.class})
public interface TerminalServiceAccessMapper {

    @Mapping(source = "service", target = "service",qualifiedByName = "toService")
    TerminalServiceAccess toModel(TerminalServiceAccessEntity entity);

    List<TerminalServiceAccess> entitiesToModels(List<TerminalServiceAccessEntity> entities);

    List<TerminalServiceAccess> entitiesToModels(Iterable<TerminalServiceAccessEntity> entities);

}
