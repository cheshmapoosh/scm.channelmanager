package ir.daneshrefah.scm.common.data.mapper;


import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface TerminalMapper {

    @Mapping(target = "legacyMaxWithdrawalPerMonth", ignore = true)
    @Mapping(target = "legacyMaxWithdrawalPerDay", ignore = true)
    Terminal toModel(TerminalEntity entity);

    TerminalEntity toEntity(Terminal model);

    List<Terminal> entitiesToModels(Iterable<TerminalEntity> entities);
}
