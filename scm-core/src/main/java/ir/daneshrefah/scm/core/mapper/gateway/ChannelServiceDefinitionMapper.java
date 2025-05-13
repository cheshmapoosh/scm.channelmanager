package ir.daneshrefah.scm.core.mapper.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ChannelServiceAccessMapper.class})
public abstract class ChannelServiceDefinitionMapper {
    @Autowired
    private ObjectMapper objectMapper;

    public abstract ChannelServiceDefinitionEntity toEntity(ChannelServiceDefinition channelServiceDefinition);

    public abstract ChannelServiceDefinition toDto(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @Mapping(target = "metadata", source = "metadata")
    public abstract RestChannelServiceDefinition toRestTypeDto(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract ChannelServiceDefinitionEntity partialUpdate(ChannelServiceDefinition channelServiceDefinition, @MappingTarget ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @AfterMapping
    protected void afterMapping(ChannelServiceDefinitionEntity entity, @MappingTarget RestChannelServiceDefinition restChannelServiceDefinition) {
        ObjectReader reader = objectMapper.readerFor(RestChannelServiceDefinition.class);
        RestChannelServiceDefinition dto= null;
        try {
            dto = reader.readValue(entity.getMetadata());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        restChannelServiceDefinition.setHttpMethod(dto.getHttpMethod());
            restChannelServiceDefinition.setPath(dto.getPath());
            List<String> pluginChains = dto.getPluginChains();
//            if (pluginChains != null && CollectionUtils.isNotEmpty(plugins)) {
//                restChannelServiceDefinition.setPluginChains(pluginChains);
//                List<Plugin> filteredPluginChain = pluginChains.stream()
//                        .map(s -> plugins.stream().filter(p -> p.getName().equals(s)).findFirst().orElse(null))
//                        .filter(Objects::nonNull).toList();
//                restChannelServiceDefinition.setPlugins(filteredPluginChain);
//            }
    }
}