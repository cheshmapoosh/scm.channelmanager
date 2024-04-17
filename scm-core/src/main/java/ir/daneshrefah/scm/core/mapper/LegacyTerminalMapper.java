package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.data.entity.terminal.LegacyTerminalEntity;
import ir.daneshrefah.scm.common.model.terminal.LegacyTerminal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LegacyTerminalMapper {
    LegacyTerminalMapper INSTANCE = Mappers.getMapper(LegacyTerminalMapper.class);

    LegacyTerminal toModel(LegacyTerminalEntity entity);

    LegacyTerminalEntity toEntity(LegacyTerminal model);
}
