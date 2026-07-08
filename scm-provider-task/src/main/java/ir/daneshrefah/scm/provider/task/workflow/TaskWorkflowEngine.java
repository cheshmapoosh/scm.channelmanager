package ir.daneshrefah.scm.provider.task.workflow;

import org.apache.camel.Exchange;

public interface TaskWorkflowEngine {
    String engineType();

    boolean supports(TaskWorkflowRole role);

    Object execute(TaskWorkflowRole role, Exchange exchange);
}
