package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskWorkflowCommandResolverTest {
    private final TaskWorkflowCommandResolver resolver = new TaskWorkflowCommandResolver();

    @Test
    void mapsCanonicalExternalActionsExplicitly() {
        assertEquals(TaskWorkflowCommand.START, resolver.resolve("start"));
        assertEquals(TaskWorkflowCommand.COMPLETE_TASK, resolver.resolve("task_complete"));
        assertEquals(TaskWorkflowCommand.APPROVE_AND_EXECUTE,
                resolver.resolve("approve_and_execute"));
    }

    @Test
    void preservesOtherSupportedActionsWithDeterministicNormalization() {
        assertEquals(TaskWorkflowCommand.CANCEL_PROCESS, resolver.resolve("cancel-process"));
        assertEquals(TaskWorkflowCommand.FIND_PROCESSES, resolver.resolve("find processes"));
        assertEquals(TaskWorkflowCommand.FIND_TASKS_BY_PROCESS_ID,
                resolver.resolve("find_tasks_by_process_id"));
        assertEquals(TaskWorkflowCommand.UPDATE_PROCESS_DESCRIPTION,
                resolver.resolve("UPDATE_PROCESS_DESCRIPTION"));
    }

    @Test
    void rejectsUnknownAction() {
        assertThrows(IllegalStateException.class, () -> resolver.resolve("complete_process"));
    }
}
