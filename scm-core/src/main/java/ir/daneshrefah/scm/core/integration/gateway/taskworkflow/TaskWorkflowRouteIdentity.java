package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import org.apache.commons.lang3.StringUtils;

public record TaskWorkflowRouteIdentity(
        String serviceCode,
        String inboundAction
) {
    public TaskWorkflowRouteIdentity {
        serviceCode = required(serviceCode, "serviceCode");
        inboundAction = required(inboundAction, "inboundAction");
    }

    private static String required(String value, String field) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            throw new TaskWorkflowRouteIdentityException(
                    "TASK_WORKFLOW route identity requires " + field);
        }
        return normalized;
    }
}
