package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.apache.camel.Exchange;

public interface TaskWorkflowEngine {
    String engineType();

    boolean supports(TaskWorkflowStepType stepType);

    Object execute(TaskWorkflowStepType stepType, Exchange exchange);
}
