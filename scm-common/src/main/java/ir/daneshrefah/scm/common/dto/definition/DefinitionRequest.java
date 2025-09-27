package ir.daneshrefah.scm.common.dto.definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ir.daneshrefah.scm.common.model.definition.DefinitionRequestDeserializer;
import ir.daneshrefah.scm.common.model.definition.DefinitionDetail;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = DefinitionRequestDeserializer.class)
public class DefinitionRequest {
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String title;
    @NotNull
    private TemplateEngineType engine;
    private List<? extends DefinitionDetail> detail;
    @NotNull
    private DefinitionType type;
}
