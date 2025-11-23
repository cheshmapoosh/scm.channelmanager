package ir.daneshrefah.scm.common.data.mapper.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.dto.definition.DefinitionDetailRequest;
import ir.daneshrefah.scm.common.dto.definition.DefinitionDetailResponse;
import ir.daneshrefah.scm.common.dto.definition.DefinitionRequest;
import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.model.definition.Definition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public abstract class DefinitionMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    public abstract DefinitionEntity toEntity(Definition definition);

    @Mapping(target = "details", expression = "java(convertToString(definitionResponse.getDetails()))")
    public abstract DefinitionEntity toEntity(DefinitionResponse definitionResponse);

    @Mapping(target = "details", expression = "java(convertToString(definitionRequest.getDetails()))")
    public abstract DefinitionEntity toEntity(DefinitionRequest definitionRequest);

    public abstract Definition toModel(DefinitionEntity definitionEntity);

    @Mapping(target = "details", expression = "java(convertToJsonNode(definitionEntity.getDetails()))")
    public abstract DefinitionResponse toDefinitionResponse(DefinitionEntity definitionEntity);

    @Mapping(target = "details", expression = "java(convertToJsonNode(definitionEntity.getDetails()))")
    public abstract DefinitionDetailResponse toDefinitionDetailResponse(DefinitionEntity definitionEntity);

    @Mapping(target = "details", expression = "java(convertToString(request.getDetails()))")
    public abstract Definition toDefinition(DefinitionDetailRequest request);


    public JsonNode convertToJsonNode(String details) {
        try {
            if (details == null) {
                return null;
            }
            return objectMapper.readTree(details);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String convertToString(JsonNode details) {
        if (details != null) {
            return details.toString();
        }
        return null;
    }
}