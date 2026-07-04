package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChainOnApproveRoutePlanFactory {
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final ChainOnApproveStepConfigExtractor stepConfigExtractor;
    private final OperationApprovalPolicyPlanFactory approvalPolicyPlanFactory;

    public ChainOnApproveRoutePlan create(Service service) {
        List<ChainOnApproveStepPlan> steps = operationSelector
                .requireAtLeastTwoActive(service, RoutingStrategy.CHAIN_ON_APPROVE)
                .stream()
                .map(operation -> createStep(service, operation))
                .sorted(Comparator.comparingInt(ChainOnApproveStepPlan::executionOrder))
                .toList();
        rejectDuplicateExecutionOrders(service, steps);
        return new ChainOnApproveRoutePlan(service, steps);
    }

    private ChainOnApproveStepPlan createStep(Service service, ServiceOperation serviceOperation) {
        ChainOnApproveStepConfig stepConfig = stepConfigExtractor.extract(service, serviceOperation);
        return new ChainOnApproveStepPlan(
                serviceOperation,
                stepConfig.executionOrder(),
                endpointResolver.resolve(serviceOperation.getOperationName()),
                approvalPolicyPlanFactory.resolvePolicy(service, serviceOperation, stepConfig),
                serviceOperation.getDefinition()
        );
    }

    private void rejectDuplicateExecutionOrders(Service service, List<ChainOnApproveStepPlan> steps) {
        ChainOnApproveStepPlan previous = null;
        for (ChainOnApproveStepPlan current : steps) {
            if (previous != null && previous.executionOrder() == current.executionOrder()) {
                throw new IllegalStateException("Cannot build CHAIN_ON_APPROVE route for serviceCode="
                        + serviceCode(service) + ", field=executionOrder: duplicate executionOrder="
                        + current.executionOrder() + " for " + describe(previous)
                        + " and " + describe(current)
                        + ". Each active service-operation in the same chain must use a unique executionOrder.");
            }
            previous = current;
        }
    }

    private String describe(ChainOnApproveStepPlan step) {
        return "operationName=" + step.serviceOperation().getOperationName()
                + ", definitionId=" + step.approvalDefinition().getId()
                + ", definitionName=" + step.approvalDefinition().getName();
    }

    private String serviceCode(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }
}
