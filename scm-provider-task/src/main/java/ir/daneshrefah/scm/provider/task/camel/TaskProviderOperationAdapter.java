package ir.daneshrefah.scm.provider.task.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngine;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngineRegistry;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowStepTypeResolver;
import org.apache.camel.Exchange;

public class TaskProviderOperationAdapter {
    private final TaskWorkflowStepTypeResolver stepTypeResolver;
    private final TaskWorkflowEngineRegistry engineRegistry;
    private final ObjectMapper objectMapper;

    public TaskProviderOperationAdapter(
            TaskWorkflowStepTypeResolver stepTypeResolver,
            TaskWorkflowEngineRegistry engineRegistry,
            ObjectMapper objectMapper
    ) {
        this.stepTypeResolver = stepTypeResolver;
        this.engineRegistry = engineRegistry;
        this.objectMapper = objectMapper;
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
        Object result = engine.execute(stepType, exchange);
        if (exchange.getProperty(Message.TASK_WORKFLOW_STEP_TYPE) != null
                && !(result instanceof Message)
                && engine.isExplicitSuccess(stepType, result)) {
            result = Message.builder()
                    .status(MessageStatus.SC_SUCCESS)
                    .payload(objectMapper.valueToTree(result))
                    .build();
        }
        exchange.getMessage().setBody(result);
    }
}
