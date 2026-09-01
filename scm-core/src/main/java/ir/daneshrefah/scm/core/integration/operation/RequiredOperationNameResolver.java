package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.service.routing.ActionDispatchPlanCatalog;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationDefinitionClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowActionPlanConfig;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowActionPlanParser;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Resolves every Operation route required by one active service plan.
 */
@Component
@RequiredArgsConstructor
public class RequiredOperationNameResolver {
    private final ActionDispatchPlanCatalog actionDispatchPlanCatalog;
    private final TaskWorkflowActionPlanParser actionPlanParser;

    public List<String> resolve(Service service) {
        if (service == null) {
            return List.of();
        }
        if (service.getRoutingStrategy() == RoutingStrategy.ACTION_DISPATCH) {
            return actionDispatchPlanCatalog.planFor(service)
                    .plansByInboundAction()
                    .values()
                    .stream()
                    .map(plan -> plan.steps().getFirst().serviceOperation().getOperationName())
                    .toList();
        }
        if (service.getServiceOperations() == null) {
            return List.of();
        }
        return service.getServiceOperations().stream()
                .filter(serviceOperation -> serviceOperation != null
                        && Boolean.TRUE.equals(serviceOperation.getActive()))
                .map(serviceOperation -> operationNames(service, serviceOperation))
                .flatMap(List::stream)
                .toList();
    }

    private List<String> operationNames(
            Service service,
            ServiceOperation serviceOperation
    ) {
        if (!ServiceOperationDefinitionClassifier.isActionPlan(serviceOperation)) {
            return List.of(serviceOperation.getOperationName());
        }
        TaskWorkflowActionPlanConfig actionPlanConfig = actionPlanParser.parse(
                service, serviceOperation);
        String inboundAction = normalizeTaskAction(actionPlanConfig.inboundAction());
        if (inboundAction.equals(TaskWorkflowCommand.APPROVE_AND_EXECUTE.name())) {
            return actionPlanConfig.steps().stream()
                    .map(step -> step.operationName())
                    .toList();
        }
        return List.of(serviceOperation.getOperationName());
    }

    private String normalizeTaskAction(String action) {
        return action.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
