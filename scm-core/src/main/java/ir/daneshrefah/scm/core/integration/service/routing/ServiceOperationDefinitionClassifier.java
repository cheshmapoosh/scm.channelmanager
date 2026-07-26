package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

public final class ServiceOperationDefinitionClassifier {
    private ServiceOperationDefinitionClassifier() {
    }

    public static boolean isActionPlan(ServiceOperation operation) {
        return operation != null
                && operation.getDefinition() != null
                && operation.getDefinition().getType()
                == DefinitionType.ACTION_PLAN;
    }
}
