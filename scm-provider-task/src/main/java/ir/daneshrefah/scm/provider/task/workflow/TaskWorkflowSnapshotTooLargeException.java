package ir.daneshrefah.scm.provider.task.workflow;

public class TaskWorkflowSnapshotTooLargeException
        extends TaskWorkflowRecoveryException {

    public TaskWorkflowSnapshotTooLargeException(
            String executionId,
            Long processId,
            int serializedSize,
            int supportedLimit
    ) {
        super("TASK_WORKFLOW snapshot exceeds watcher DATA capacity "
                + "executionId=" + executionId
                + ", processId=" + processId
                + ", serializedCharacters=" + serializedSize
                + ", supportedCharacterLimit=" + supportedLimit);
    }
}
