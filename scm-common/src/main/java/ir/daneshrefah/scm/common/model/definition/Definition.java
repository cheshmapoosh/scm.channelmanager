package ir.daneshrefah.scm.common.model.definition;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.template.TemplateEngine;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.definition.DefinitionEntity}
 */
@Getter
@Setter
public class Definition extends AbstractAuditableModel<String> {
    private String id;
    private String title;
    private String name;
    private TemplateEngine engine;
    @Size(max = 2048)
    private String details;

}