package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public enum TaskWorkflowCommand {
    START,
    COMPLETE_TASK,
    APPROVE_AND_EXECUTE,
    CANCEL_PROCESS,
    FIND_PROCESSES,
    FIND_TASKS,
    FIND_TASKS_BY_PROCESS_ID,
    UPDATE_PROCESS_DESCRIPTION
}
