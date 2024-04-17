package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TerminalMapper {
    TerminalMapper INSTANCE = Mappers.getMapper(TerminalMapper.class);

    Terminal toModel(TerminalEntity entity);
    TerminalEntity toEntity(Terminal model);

    List<Terminal> entitiesToModels(Iterable<TerminalEntity> entities);
}
