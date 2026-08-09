package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public enum TaskWorkflowCommand {
    START_PROCESS,
    TASK_COMPLETE,
    APPROVE_AND_EXECUTE,
    REJECT_PROCESS,
    GET_ALL_PROCESS,
    GET_ALL_TASK,
    GET_TASK,
    UPDATE_DESCRIPTION,
    DELETE_PROCUREMENT,
    FIND_PROCUREMENT_BY_ACCOUNT,
    FIND_PROCUREMENT_BY_NATIONAL,
    PROCUREMENT_STATEMENT_INQUIRY
}
