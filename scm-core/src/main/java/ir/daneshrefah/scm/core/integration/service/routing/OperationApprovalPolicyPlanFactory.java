package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationApprovalPolicyPlanFactory {
    private final OperationApprovalPolicyRegistry policyRegistry;
    private final DefaultOperationApprovalPolicy defaultPolicy;

    public OperationApprovalPolicy resolvePolicy(
            Service service,
            ServiceOperation serviceOperation,
            ChainOnApproveStepConfig stepConfig
    ) {
        Definition definition = serviceOperation == null ? null : serviceOperation.getDefinition();
        if (stepConfig == null) {
            throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for serviceCode="
                    + serviceCode(service) + ", operationName=" + operationName(serviceOperation)
                    + ", definitionId=" + definitionId(definition)
                    + ", definitionName=" + definitionName(definition)
                    + ", field=details: extracted step config must not be null");
        }

        String policyCode = stepConfig.approvalPolicyCode();
        if (StringUtils.isBlank(policyCode)) {
            return defaultPolicy;
        }

        try {
            return policyRegistry.getRequired(policyCode);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for serviceCode="
                    + serviceCode(service) + ", operationName=" + operationName(serviceOperation)
                    + ", definitionId=" + definitionId(definition)
                    + ", definitionName=" + definitionName(definition)
                    + ", field=approvalPolicyCode: " + exception.getMessage(), exception);
        }
    }

    private String serviceCode(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }

    private String operationName(ServiceOperation serviceOperation) {
        return serviceOperation == null ? "<null>" : String.valueOf(serviceOperation.getOperationName());
    }

    private String definitionId(Definition definition) {
        return definition == null ? "<null>" : String.valueOf(definition.getId());
    }

    private String definitionName(Definition definition) {
        return definition == null ? "<null>" : String.valueOf(definition.getName());
    }
}
