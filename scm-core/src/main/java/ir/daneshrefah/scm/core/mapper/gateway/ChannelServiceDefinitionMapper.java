package ir.daneshrefah.scm.core.mapper.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

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
    protected void afterMappingRestChannelServiceDefinition(ChannelServiceDefinitionEntity entity, @MappingTarget RestChannelServiceDefinition dto) {
        ObjectReader reader = objectMapper.readerFor(RestChannelServiceDefinition.class);
        try {
            RestChannelServiceDefinition dtoFromMetadata= reader.readValue(entity.getMetadata());
            dto.setHttpMethod(dtoFromMetadata.getHttpMethod());
            dto.setPath(dtoFromMetadata.getPath());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}