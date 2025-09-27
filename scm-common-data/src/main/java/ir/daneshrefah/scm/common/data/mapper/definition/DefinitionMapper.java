package ir.daneshrefah.scm.common.data.mapper.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.dto.definition.*;
import ir.daneshrefah.scm.common.model.definition.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public abstract class DefinitionMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    public abstract DefinitionEntity toEntity(Definition definition);

    public abstract DefinitionEntity toEntity(DefinitionResponse definitionResponse);

    @Mapping(target = "details", expression = "java(mapToString(definitionRequest.getDetail()))")
    public abstract DefinitionEntity toEntity(DefinitionRequest definitionRequest);

    public abstract Definition toModel(DefinitionEntity definitionEntity);

    @Mapping(target = "detail", expression = "java(mapDetails(definitionEntity))")
    public abstract DefinitionResponse toDefinitionResponse(DefinitionEntity definitionEntity);

    @Mapping(target = "detail", expression = "java(mapDetails(definitionEntity))")
    public abstract DefinitionDetailResponse toDefinitionDetailResponse(DefinitionEntity definitionEntity);

    @Mapping(target = "details", expression = "java(mapToString(request.getDetail()))")
    public abstract Definition toDefinition(DefinitionDetailRequest request);

    List<? extends DefinitionDetail> mapDetails(DefinitionEntity entity) {
        try {
            if (entity == null || entity.getType() == null || entity.getDetails() == null) {
                return null;
            }
            return switch (entity.getType()) {
                case PLUGIN ->
                        objectMapper.readValue(entity.getDetails(), new TypeReference<List<PluginDefinitionDetail>>() {
                        });
                default -> throw new Exception("Invalid entity type");
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    String mapToString(List<? extends DefinitionDetail> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}