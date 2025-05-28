package ir.daneshrefah.scm.core.mapper.plugin;

import ir.daneshrefah.scm.common.model.plugin.Plugin;
import ir.daneshrefah.scm.core.entity.plugin.PluginEntity;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING, uses = {DefinitionMapper.class})
public interface PluginMapper {
    PluginEntity toEntity(Plugin plugin);

    Plugin toModel(PluginEntity pluginEntity);

 }