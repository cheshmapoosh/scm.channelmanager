package ir.daneshrefah.scm.common.event.provider;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmProviderEventType implements ScmEventType {
    PROVIDER_REQUEST_SENT("provider.request.sent"),
    PROVIDER_RESPONSE_RECEIVED("provider.response.received"),
    PROVIDER_CALL_FAILED("provider.call.failed"),
    PROVIDER_TIMEOUT("provider.timeout"),

    PROCESS_START_REQUESTED("task.process.start.requested"),
    PROCESS_STARTED("task.process.started"),
    PROCESS_APPROVE_REQUESTED("task.process.approve.requested"),
    PROCESS_APPROVED("task.process.approved"),
    PROCESS_COMPLETE_REQUESTED("task.process.complete.requested"),
    PROCESS_COMPLETED("task.process.completed"),
    PROCESS_FAILED("task.process.failed"),
    PROCESS_CANCEL_REQUESTED("task.process.cancel.requested"),
    PROCESS_CANCELLED("task.process.cancelled"),

    TASK_COMPLETE_REQUESTED("task.task.complete.requested"),
    TASK_COMPLETED("task.task.completed"),
    TASK_COMPLETE_FAILED("task.task.complete.failed"),

    WORKFLOW_BUSINESS_STARTED("task.workflow.business.started"),
    WORKFLOW_BUSINESS_SUCCEEDED("task.workflow.business.succeeded"),
    WORKFLOW_BUSINESS_FAILED("task.workflow.business.failed"),
    WORKFLOW_BUSINESS_UNKNOWN("task.workflow.business.unknown");

    private final String code;

    ScmProviderEventType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
