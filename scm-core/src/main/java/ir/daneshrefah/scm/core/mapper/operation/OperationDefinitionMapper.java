package ir.daneshrefah.scm.core.mapper.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.model.operation.OperationDefinition;
import ir.daneshrefah.scm.common.model.operation.RequestTemplateOperationDefinition;
import ir.daneshrefah.scm.common.model.operation.ResponseTemplateOperationDefinition;
import ir.daneshrefah.scm.common.model.operation.RestConfigOperationDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.entity.operation.OperationDefinitionEntity;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import ir.daneshrefah.scm.utils.string.JsonPathFinder;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OperationMapper.class, DefinitionMapper.class})
public abstract class OperationDefinitionMapper {
    @Autowired
    private ObjectMapper mapper;
    private ObjectReader reader;

    @PostConstruct
    public void init() {
        this.reader = mapper.reader();
    }


    public abstract OperationDefinitionEntity toEntity(OperationDefinition operationDefinition);

    @Named("toDto")
    public OperationDefinition toDto(OperationDefinitionEntity operationDefinitionEntity) {
        return switch (operationDefinitionEntity.getType()) {
            case REQUEST_TEMPLATE -> toRequestTemplateDto(operationDefinitionEntity);
            case RESPONSE_TEMPLATE -> toResponseTemplateDto(operationDefinitionEntity);
            case REST_CONFIG -> toRestConfigDto(operationDefinitionEntity);
            default -> throw new IllegalStateException("Unexpected value: " + operationDefinitionEntity.getType());
        };
    }

    @Named("responseTemplate")
    public abstract ResponseTemplateOperationDefinition toResponseTemplateDto(OperationDefinitionEntity operationDefinitionEntity);

    @Named("requestTemplate")
    public abstract RequestTemplateOperationDefinition toRequestTemplateDto(OperationDefinitionEntity operationDefinitionEntity);

    @Named("restConfig")
    public abstract RestConfigOperationDefinition toRestConfigDto(OperationDefinitionEntity operationDefinitionEntity);

    @AfterMapping
    public void afterMapping(@MappingTarget RequestTemplateOperationDefinition requestTemplateOperationDefinition) {
//        try {
//            RequestTemplateOperationDefinition dto = dtoReader.readValue(requestTemplateOperationDefinition.getDefinition().getDetails());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }

    @AfterMapping
    public void afterMapping(@MappingTarget ResponseTemplateOperationDefinition responseTemplateOperationDefinition) {
//        try {
//            ResponseTemplateOperationDefinition dto = dtoReader.readValue(responseTemplateOperationDefinition.getDefinition().getDetails());
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
    }

    @AfterMapping
    public void afterMapping(@MappingTarget RestConfigOperationDefinition restConfigOperationDefinition) {
        try {
            JsonNode dtoNode = reader.readTree(restConfigOperationDefinition.getDefinition().getDetails());
            
            String url = JsonPathFinder.defaultAsText(dtoNode, "url");
            restConfigOperationDefinition.setUrl(url);
            
            String method = JsonPathFinder.defaultAsText(dtoNode, "method");
            restConfigOperationDefinition.setHttpMethod(HttpMethod.fromValue(method));

            Integer responseTimeout = JsonPathFinder.defaultAsInteger(dtoNode, "responseTimeout");
            restConfigOperationDefinition.setResponseTimeout(responseTimeout);

            Integer connectTimeout = JsonPathFinder.defaultAsInteger(dtoNode, "connectTimeout");
            restConfigOperationDefinition.setConnectTimeout(connectTimeout);

            Integer writeTimeout = JsonPathFinder.defaultAsInteger(dtoNode, "writeTimeout");
            restConfigOperationDefinition.setWriteTimeout(writeTimeout);

            Boolean retryEnabled = JsonPathFinder.defaultAsBoolean(dtoNode, "retryEnabled");
            restConfigOperationDefinition.setRetryEnabled(retryEnabled);

            Integer maxAttempts = JsonPathFinder.defaultAsInteger(dtoNode, "maxAttempts");
            restConfigOperationDefinition.setMaxAttempts(maxAttempts);

            Integer minBackoff = JsonPathFinder.defaultAsInteger(dtoNode, "minBackoff");
            restConfigOperationDefinition.setMinBackoff(minBackoff);

            Boolean wiretap = JsonPathFinder.defaultAsBoolean(dtoNode, "wiretap");
            restConfigOperationDefinition.setWiretap(wiretap);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}