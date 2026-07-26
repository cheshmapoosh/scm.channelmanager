package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRouteContext;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRoutingHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskWorkflowServiceTargetRoutingHandler
        implements ServiceTargetRoutingHandler {

    private final ObjectProvider<TaskWorkflowRuntime> runtimeProvider;

    public TaskWorkflowServiceTargetRoutingHandler(
            ObjectProvider<TaskWorkflowRuntime> runtimeProvider
    ) {
        this.runtimeProvider = runtimeProvider;
    }

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.TASK_WORKFLOW;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        List<TaskWorkflowRuntime> runtimes =
                runtimeProvider.orderedStream().toList();
        if (runtimes.size() != 1) {
            throw new IllegalStateException(
                    "Active TASK_WORKFLOW serviceCode="
                            + context.service().getCode()
                            + " requires exactly one task-provider core "
                            + "integration runtime; found " + runtimes.size()
                            + ". The provider capability or recovery store is "
                            + "missing or duplicated."
            );
        }
        runtimes.getFirst().buildTarget(context);
    }
}
