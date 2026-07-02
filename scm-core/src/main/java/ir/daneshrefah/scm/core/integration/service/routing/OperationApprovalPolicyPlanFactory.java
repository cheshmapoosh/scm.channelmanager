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
    private final OperationApprovalPolicyCodeExtractor policyCodeExtractor;

    public OperationApprovalPolicy resolvePolicy(Service service, ServiceOperation serviceOperation) {
        Definition definition = serviceOperation.getDefinition();
        try {
            String policyCode = policyCodeExtractor.extract(definition);
            if (StringUtils.isBlank(policyCode)) {
                return defaultPolicy;
            }
            return policyRegistry.getRequired(policyCode);
        } catch (IllegalStateException exception) {
            throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for service "
                    + serviceCode(service) + ", operation " + serviceOperation.getOperationName()
                    + ": " + exception.getMessage(), exception);
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
