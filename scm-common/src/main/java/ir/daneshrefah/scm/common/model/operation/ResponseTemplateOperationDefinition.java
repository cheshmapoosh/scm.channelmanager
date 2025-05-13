package ir.daneshrefah.scm.common.model.operation;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseTemplateOperationDefinition extends OperationDefinition {

    @Override
    public OperationDefinitionType getType() {
        return OperationDefinitionType.RESPONSE_TEMPLATE;
    }
}
