package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.apache.camel.Exchange;

public class TaskWorkflowStepTypeResolver {

    public TaskWorkflowStepType resolve(String providerCode, Exchange exchange) {
        if (exchange == null) {
            throw new IllegalArgumentException("scm-task:" + providerCode
                    + " requires a Camel exchange.");
        }
        Object value = exchange.getProperty(Message.TASK_WORKFLOW_STEP_TYPE);
        if (value == null) {
            throw new IllegalArgumentException("scm-task:" + providerCode
                    + " requires Message.TASK_WORKFLOW_STEP_TYPE as TaskWorkflowStepType.");
        }
        if (!(value instanceof TaskWorkflowStepType)) {
            throw new IllegalArgumentException("Invalid "
                    + Message.TASK_WORKFLOW_STEP_TYPE + " type for scm-task:" + providerCode
                    + "; expected " + TaskWorkflowStepType.class.getName()
                    + " but was " + value.getClass().getName());
        }
        return exchange.getProperty(
                Message.TASK_WORKFLOW_STEP_TYPE,
                TaskWorkflowStepType.class
        );
    }
}
