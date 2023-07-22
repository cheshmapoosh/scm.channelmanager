package ir.daneshrefah.scm.mapper;

import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.entity.terminal.TerminalServiceChannelAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TerminalServiceChannelAccessMapper {

    TerminalServiceChannelAccessMapper INSTANCE = Mappers.getMapper(TerminalServiceChannelAccessMapper.class);

//    @Mapping(source = "terminalServiceAccessEntity", target = "terminalServiceAccess")
//    @Mapping(source = "channelEntity", target = "channel")
//    TerminalServiceChannelAccess toModel(TerminalServiceChannelAccessEntity entity);
//
//    List<TerminalServiceChannelAccess> entitiesToModels(List<TerminalServiceChannelAccessEntity> entities);

}
