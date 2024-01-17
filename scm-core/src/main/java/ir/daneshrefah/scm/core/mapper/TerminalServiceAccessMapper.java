package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TerminalServiceAccessMapper {

    TerminalServiceAccessMapper INSTANCE = Mappers.getMapper(TerminalServiceAccessMapper.class);

    @Mapping(source = "service", target = "service", qualifiedByName = "mapService")
    TerminalServiceAccess toModel(TerminalServiceAccessEntity entity);

    @Mapping(source = "service", target = "service")
//    @Mapping(source = "terminalEntity", target = "terminal")
    List<TerminalServiceAccess> entitiesToModels(List<TerminalServiceAccessEntity> entities);

    @Named("mapService")
    default Service mapService(ServiceEntity entity) {
        // Delegate the mapping to the method in ServiceMapper
        return ServiceMapper.INSTANCE.toService(entity);
    }
}
