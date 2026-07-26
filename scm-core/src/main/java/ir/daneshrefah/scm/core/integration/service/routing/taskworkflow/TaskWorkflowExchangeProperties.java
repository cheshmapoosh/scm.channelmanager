package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public final class TaskWorkflowExchangeProperties {
    public static final String COMMAND = "scmTaskWorkflowCommand";
    public static final String COMMAND_PLAN = "scmTaskWorkflowCommandPlan";
    public static final String APPROVE_RESPONSE = "scmTaskWorkflowApproveResponse";
    public static final String BUSINESS_RESPONSE = "scmTaskWorkflowBusinessResponse";
    public static final String BUSINESS_RESULT = "scmTaskWorkflowBusinessResult";
    public static final String BUSINESS_RESULT_STATUS = "scmTaskWorkflowBusinessResultStatus";
    public static final String PROCESS_ID = "scmTaskWorkflowProcessId";
    public static final String TASK_ID = "scmTaskWorkflowTaskId";
    public static final String CORRELATION_ID = "scmTaskWorkflowCorrelationId";
    public static final String PROVIDER_IDEMPOTENCY_KEY =
            "scmTaskWorkflowProviderIdempotencyKey";

    private TaskWorkflowExchangeProperties() {
    }
}
