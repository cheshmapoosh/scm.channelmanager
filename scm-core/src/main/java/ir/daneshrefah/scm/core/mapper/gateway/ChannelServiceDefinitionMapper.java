package ir.daneshrefah.scm.core.mapper.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.utils.string.JsonPathFinder;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING, uses = {ChannelServiceAccessMapper.class})
public abstract class ChannelServiceDefinitionMapper {
    @Autowired
    private ObjectMapper mapper;
    private ObjectReader dtoReader;

    @PostConstruct
    public void init() {
        this.dtoReader = mapper.reader();
    }


    public abstract ChannelServiceDefinitionEntity toEntity(ChannelServiceDefinition channelServiceDefinition);

    @Named("toModel")
    public  ChannelServiceDefinition toModel(ChannelServiceDefinitionEntity channelServiceDefinitionEntity) {
        return switch (channelServiceDefinitionEntity.getType()) {
            case REST -> toRestDto(channelServiceDefinitionEntity);
            case SWAGGER -> throw new IllegalStateException("Unexpected value: " + channelServiceDefinitionEntity.getType());
        };
    }

    @Named("toRestDto")
    public abstract RestChannelServiceDefinition toRestDto(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @AfterMapping
    protected void afterMapping(@MappingTarget RestChannelServiceDefinition restChannelServiceDefinition) {
        JsonNode dtoNode = null;
        try {
            dtoNode = dtoReader.readTree(restChannelServiceDefinition.getDefinition().getDetails());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String path = JsonPathFinder.defaultAsText(dtoNode, "path");
        restChannelServiceDefinition.setPath(path);

        String method = JsonPathFinder.defaultAsText(dtoNode, "method");
        restChannelServiceDefinition.setMethod(HttpMethod.fromValue(method));


    }
}