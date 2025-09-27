package ir.daneshrefah.scm.common.dto.definition;

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
    private List<? extends DefinitionDetail> detail;
    private DefinitionType type;
}