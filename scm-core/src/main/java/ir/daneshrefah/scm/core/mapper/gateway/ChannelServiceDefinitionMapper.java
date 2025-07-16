package ir.daneshrefah.scm.core.mapper.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RestMultipleChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.SwggerChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import ir.daneshrefah.scm.utils.string.JsonPathFinder;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public ChannelServiceDefinition toModel(ChannelServiceDefinitionEntity channelServiceDefinitionEntity) {
        return switch (channelServiceDefinitionEntity.getType()) {
            case REST -> toRest(channelServiceDefinitionEntity);
            case SWAGGER -> toSwagger(channelServiceDefinitionEntity);
            case REST_MULTIPLE -> toRestMultiple(channelServiceDefinitionEntity);
        };
    }

    @Named("toRestMultiple")
    public abstract RestMultipleChannelServiceDefinition toRestMultiple(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @Named("toRest")
    public abstract RestChannelServiceDefinition toRest(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @Named("toSwagger")
    public abstract SwggerChannelServiceDefinition toSwagger(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    @AfterMapping
    protected void afterMapping(@MappingTarget RestChannelServiceDefinition restChannelServiceDefinition) {
        enrichRestChannelServiceDefinition(restChannelServiceDefinition);
    }

    @AfterMapping
    public void afterMapping(@MappingTarget RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
        enrichRestMultipleChannelServiceDefinition(restMultipleChannelServiceDefinition);
    }

    public void enrichRestMultipleChannelServiceDefinition(RestMultipleChannelServiceDefinition restMultipleChannelServiceDefinition) {
        JsonNode dtoNode;
        try {
            dtoNode = dtoReader.readTree(restMultipleChannelServiceDefinition.getDefinition().getDetails());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String contextPath = JsonPathFinder.defaultAsText(dtoNode, "contextPath");
        restMultipleChannelServiceDefinition.setContextPath(contextPath);
        JsonNode definitionsNode = JsonPathFinder.defaultNode(dtoNode, "definitions");
        List<String> definitions = new ArrayList<>();
        if (Objects.nonNull(definitionsNode) && definitionsNode.isArray()) {
            for (JsonNode defNode : definitionsNode) {
                definitions.add(defNode.asText());
            }
        }
        restMultipleChannelServiceDefinition.setDefinitionIdList(definitions);
    }

    public void enrichRestChannelServiceDefinition(RestChannelServiceDefinition restChannelServiceDefinition) {
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