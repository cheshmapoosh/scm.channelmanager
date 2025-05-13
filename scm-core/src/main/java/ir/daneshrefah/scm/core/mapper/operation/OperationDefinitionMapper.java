package ir.daneshrefah.scm.core.mapper.operation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.model.operation.OperationDefinition;
import ir.daneshrefah.scm.common.model.operation.OperationDefinitionType;
import ir.daneshrefah.scm.common.model.operation.RequestTemplateOperationDefinition;
import ir.daneshrefah.scm.common.model.operation.ResponseTemplateOperationDefinition;
import ir.daneshrefah.scm.core.entity.operation.OperationDefinitionEntity;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {OperationMapper.class, DefinitionMapper.class})
public abstract class OperationDefinitionMapper {
    @Autowired
    private ObjectMapper objectMapper;

    public abstract OperationDefinitionEntity toEntity(OperationDefinition operationDefinition);

    public OperationDefinition toDto(OperationDefinitionEntity operationDefinitionEntity) {
        if (Objects.equals(OperationDefinitionType.REQUEST_TEMPLATE, operationDefinitionEntity.getType())) {
            return toRequestTemplateDto(operationDefinitionEntity);
        }
        if (Objects.equals(OperationDefinitionType.RESPONSE_TEMPLATE, operationDefinitionEntity.getType())) {
            return toResponseTemplateDto(operationDefinitionEntity);
        }
        throw new IllegalArgumentException("Unsupported operation definition type: " + operationDefinitionEntity.getType());
    }

    public abstract ResponseTemplateOperationDefinition toResponseTemplateDto(OperationDefinitionEntity operationDefinitionEntity);

    public abstract RequestTemplateOperationDefinition toRequestTemplateDto(OperationDefinitionEntity operationDefinitionEntity);

    @AfterMapping
    public void afterMapping(OperationDefinitionEntity operationDefinitionEntity, @MappingTarget RequestTemplateOperationDefinition requestTemplateOperationDefinition) {
        ObjectReader reader = objectMapper.readerFor(RequestTemplateOperationDefinition.class);
        try {
            RequestTemplateOperationDefinition dto = reader.readValue(operationDefinitionEntity.getDefinition().getDetails());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    @AfterMapping
    public void afterMapping(OperationDefinitionEntity operationDefinitionEntity, @MappingTarget ResponseTemplateOperationDefinition responseTemplateOperationDefinition) {
        ObjectReader reader = objectMapper.readerFor(RequestTemplateOperationDefinition.class);
        try {
            ResponseTemplateOperationDefinition dto = reader.readValue(operationDefinitionEntity.getDefinition().getDetails());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
 }