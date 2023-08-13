package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceChannelAccessEntity;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TerminalServiceChannelAccessMapper {

    TerminalServiceChannelAccessMapper INSTANCE = Mappers.getMapper(TerminalServiceChannelAccessMapper.class);

    @Mapping(source = "terminalServiceAccessEntity", target = "terminalServiceAccess")
    @Mapping(source = "terminalServiceAccessEntity.serviceEntity", target = "terminalServiceAccess.service", qualifiedByName = "mapService")
    @Mapping(source = "terminalServiceAccessEntity.terminalEntity", target = "terminalServiceAccess.terminal")
    @Mapping(source = "channelEntity", target = "channel")
    TerminalServiceChannelAccess toModel(TerminalServiceChannelAccessEntity entity);

    List<TerminalServiceChannelAccess> entitiesToModels(List<TerminalServiceChannelAccessEntity> entities);

    @Named("mapService")
    default Service mapService(ServiceEntity entity) {
        // Delegate the mapping to the method in ServiceMapper
        return ServiceMapper.INSTANCE.toService(entity);
    }

}
