package ir.daneshrefah.scm.core.mapper.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.dto.channelServiceDefination.ChannelServiceDefinitionResponse;
import ir.daneshrefah.scm.common.model.gateway.*;
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
            case INBOUND_ROUTE, REST -> toRest(channelServiceDefinitionEntity);
            case INBOUND_ROUTE_GROUP, REST_MULTIPLE -> toRestMultiple(channelServiceDefinitionEntity);
            case API_DOCUMENTATION, SWAGGER -> toSwagger(channelServiceDefinitionEntity);
            case SERVICE_DOMAIN_MEMBER -> channelServiceDefinitionEntityToModel(channelServiceDefinitionEntity);
        };
    }

    public abstract ChannelServiceDefinition channelServiceDefinitionEntityToModel(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

    public abstract ChannelServiceDefinitionResponse toChannelServiceDefinition(ChannelServiceDefinitionEntity channelServiceDefinitionEntity);

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
        JsonNode definitionsNode = JsonPathFinder.defaultNode(dtoNode, "multiRouteDetails");
        List<RestMultipleChannelServiceDefinition.MultiRouteDetail> multiRouteDetails = new ArrayList<>();
        if (Objects.nonNull(definitionsNode) && definitionsNode.isArray()) {
            for (JsonNode defNode : definitionsNode) {
                RestMultipleChannelServiceDefinition.MultiRouteDetail multiRouteDetail = new RestMultipleChannelServiceDefinition.MultiRouteDetail();
                multiRouteDetail.setDefinitionId(defNode.get("definitionId").asText());
                multiRouteDetail.setOperationCode(defNode.get("operationCode").asText());
                multiRouteDetails.add(multiRouteDetail);
            }
        }
        restMultipleChannelServiceDefinition.setMultiRouteDetails(multiRouteDetails);
    }

    public void enrichRestChannelServiceDefinition(RestChannelServiceDefinition restChannelServiceDefinition) {
        JsonNode dtoNode;
        try {
            dtoNode = dtoReader.readTree(restChannelServiceDefinition.getDefinition().getDetails());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // path
        String path = JsonPathFinder.defaultAsText(dtoNode, "path");
        restChannelServiceDefinition.setPath(path);

        // method
        String method = JsonPathFinder.defaultAsText(dtoNode, "method");
        restChannelServiceDefinition.setMethod(HttpMethod.fromValue(method));

        // checkLoginAuthentication
        Boolean checkLoginAuthentication = JsonPathFinder.defaultAsBoolean(dtoNode, "checkLoginAuthentication");
        restChannelServiceDefinition.setCheckLoginAuthentication(
                checkLoginAuthentication != null ? checkLoginAuthentication : false
        );

        // authorizationConfig
        JsonNode authorizationConfigNode = dtoNode.get("authorizationConfig");
        if (authorizationConfigNode != null && !authorizationConfigNode.isNull()) {
            BaseChannelServiceDefinition.AuthorizationConfig authorizationConfig =
                    new BaseChannelServiceDefinition.AuthorizationConfig();

            // chain
            String chain = JsonPathFinder.defaultAsText(authorizationConfigNode, "chain");
            authorizationConfig.setChain(chain);

            // accessRoles
            List<String> accessRoles = new ArrayList<>();
            JsonNode rolesNode = authorizationConfigNode.get("accessRoles");
            if (rolesNode != null && rolesNode.isArray()) {
                for (JsonNode roleNode : rolesNode) {
                    accessRoles.add(roleNode.asText());
                }
            }
            authorizationConfig.setAccessRoles(accessRoles);

            // authorities list
            List<String> authorities = new ArrayList<>();
            JsonNode authoritiesNode = authorizationConfigNode.get("authorities");
            if (authoritiesNode != null && authoritiesNode.isArray()) {
                for (JsonNode authorityNode : authoritiesNode) {
                    authorities.add(authorityNode.asText());
                }
            }
            authorizationConfig.setAuthorities(authorities);


            restChannelServiceDefinition.setAuthorizationConfig(authorizationConfig);
        } else {
            restChannelServiceDefinition.setAuthorizationConfig(null);
        }
    }
}
