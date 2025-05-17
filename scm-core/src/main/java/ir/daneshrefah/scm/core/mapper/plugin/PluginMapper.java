package ir.daneshrefah.scm.core.mapper.plugin;

import ir.daneshrefah.scm.common.model.plugin.Plugin;
import ir.daneshrefah.scm.core.entity.plugin.PluginEntity;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {DefinitionMapper.class})
public interface PluginMapper {
    PluginEntity toEntity(Plugin plugin);

    Plugin toDto(PluginEntity pluginEntity);

 }