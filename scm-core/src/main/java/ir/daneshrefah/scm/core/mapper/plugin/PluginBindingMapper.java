package ir.daneshrefah.scm.core.mapper.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingSearchResponse;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.plugin.PluginBinding;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingCreateRequest;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingResponse;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.core.entity.plugin.PluginBindingEntity;
import ir.daneshrefah.scm.common.data.mapper.definition.DefinitionMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING,
        uses = {PluginMapper.class, DefinitionMapper.class})
public abstract class PluginBindingMapper {

    @Autowired
    private ObjectMapper mapper;
    private ObjectReader pluginAdvisorsReader;

    @PostConstruct
    public void init() {
        this.pluginAdvisorsReader = mapper.readerFor(new TypeReference<List<PluginDetail>>() {
        });
    }

    public abstract PluginBindingEntity toEntity(PluginBinding pluginBinding);

    public abstract PluginBindingEntity toEntity(PluginBindingCreateRequest pluginBindingCreateRequest);

    public abstract PluginBinding toModel(PluginBindingEntity pluginBindingEntity);

    @Mapping(target = "definitionId", source = "definition.id")
    public abstract PluginBindingResponse toPluginBindingResponse(PluginBindingEntity pluginBindingEntity);

    public abstract PluginBindingSearchResponse toPluginBindingCreateResponse(PluginBindingEntity pluginBindingEntity);

    @AfterMapping
    public void afterMapping(@MappingTarget PluginBinding pluginBinding) {
        List<PluginDetail> pluginDetails;
        try {
            Definition definition = pluginBinding.getDefinition();
            if (definition != null) {
                pluginDetails = pluginAdvisorsReader.readValue(definition.getDetails());
                pluginBinding.setDetails(pluginDetails);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}