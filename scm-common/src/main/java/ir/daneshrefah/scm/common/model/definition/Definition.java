package ir.daneshrefah.scm.common.model.definition;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.template.TemplateEngineType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Definition extends AbstractAuditableModel<String> {
    private String id;
    private String title;
    private String name;
    private TemplateEngineType engine;
    @Size(max = 2048)
    private String details;
    private DefinitionType type;
}
