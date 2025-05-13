package ir.daneshrefah.scm.common.model.operation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestTemplateOperationDefinition extends OperationDefinition {

    @Override
    public OperationDefinitionType getType() {
        return OperationDefinitionType.REQUEST_TEMPLATE;
    }

}
