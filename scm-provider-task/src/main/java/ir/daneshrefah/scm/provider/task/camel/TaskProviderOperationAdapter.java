package ir.daneshrefah.scm.provider.task.camel;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngine;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngineRegistry;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowStepTypeResolver;
import org.apache.camel.Exchange;

public class TaskProviderOperationAdapter {
    private final TaskWorkflowStepTypeResolver stepTypeResolver;
    private final TaskWorkflowEngineRegistry engineRegistry;

    public TaskProviderOperationAdapter(
            TaskWorkflowStepTypeResolver stepTypeResolver,
            TaskWorkflowEngineRegistry engineRegistry
    ) {
        this.stepTypeResolver = stepTypeResolver;
        this.engineRegistry = engineRegistry;
    }

    public String endpointProviderCode(String remaining) {
        return engineRegistry.requireProviderCode(remaining);
    }

    public void execute(String providerCode, Exchange exchange) {
        TaskWorkflowStepType stepType = stepTypeResolver.resolve(providerCode, exchange);
        TaskWorkflowEngine engine = engineRegistry.engineFor(providerCode);
        if (!engine.supports(stepType)) {
            throw new IllegalArgumentException("scm-task:" + providerCode
                    + " engine-type=" + engine.engineType()
                    + " does not support stepType=" + stepType);
        }
        exchange.getMessage().setBody(engine.execute(stepType, exchange));
    }
}
