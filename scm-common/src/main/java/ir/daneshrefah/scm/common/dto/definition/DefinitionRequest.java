package ir.daneshrefah.scm.common.dto.definition;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionRequest {
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String title;
    @NotNull
    private TemplateEngineType engine;
    private JsonNode details;
    @NotNull
    private DefinitionType type;
}
