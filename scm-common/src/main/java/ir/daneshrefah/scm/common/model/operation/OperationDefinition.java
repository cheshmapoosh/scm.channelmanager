package ir.daneshrefah.scm.common.model.operation;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.definition.Definition;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.operation.OperationDefinitionEntity}
 */
@Getter
@Setter
public class OperationDefinition extends AbstractAuditableModel<String> {
    private String id;
    private OperationDefinitionType type;
    private Definition definition;
}