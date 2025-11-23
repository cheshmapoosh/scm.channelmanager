package ir.daneshrefah.scm.common.dto.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.definition.DefinitionDetail;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DefinitionResponse {
    private String id;
    private String name;
    private String title;
    private TemplateEngineType engine;
    private JsonNode details;
    private DefinitionType type;
    @JsonIgnore
    private Integer version;
}