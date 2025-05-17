package ir.daneshrefah.scm.core.mapper.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.model.plugin.PluginDefinition;
import ir.daneshrefah.scm.common.plugin.PluginAdvice;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {PluginMapper.class, DefinitionMapper.class})
public abstract class PluginBindingMapper {

    @Autowired
    private ObjectMapper mapper;
    @Autowired(required = false)
    private Map<String, PluginAdvice> pluginAdvices;

    private ObjectReader pluginAdvisorsReader;

    @PostConstruct
    public void init() {
        this.pluginAdvisorsReader = mapper.readerFor(new TypeReference<List<PluginDefinition>>() {
        });
    }

    public abstract PluginBindingEntity toEntity(PluginBinding pluginBinding);

    public abstract PluginBinding toDto(PluginBindingEntity pluginBindingEntity);

    @AfterMapping
    public void afterMapping(@MappingTarget PluginBinding pluginBinding) {
        List<PluginDefinition> pluginDefinitions;
        try {
            pluginDefinitions = pluginAdvisorsReader.readValue(pluginBinding.getDefinition().getDetails());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        pluginDefinitions.forEach(pluginDefinition -> pluginDefinition.setPluginAdvice(pluginAdvices.get(pluginDefinition.getName())));
        pluginBinding.setAdvisors(pluginDefinitions);
    }

}