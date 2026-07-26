package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.commons.lang3.StringUtils;

public record RoutingPlanIdentity(
        String serviceCode,
        String inboundAction,
        String actionPlanName,
        String definitionId,
        String planFingerprint
) {
    public RoutingPlanIdentity {
        serviceCode = required(serviceCode, "serviceCode");
        inboundAction = required(inboundAction, "inboundAction");
        actionPlanName = required(actionPlanName, "actionPlanName");
        definitionId = required(definitionId, "definitionId");
        planFingerprint = required(planFingerprint, "planFingerprint");
    }

    private static String required(String value, String field) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
