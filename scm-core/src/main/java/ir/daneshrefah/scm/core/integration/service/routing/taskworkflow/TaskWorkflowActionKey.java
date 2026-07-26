package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public record TaskWorkflowActionKey(
        String serviceCode,
        String inboundAction
) {
    public TaskWorkflowActionKey {
        serviceCode = normalize(serviceCode, "serviceCode");
        inboundAction = normalize(inboundAction, "inboundAction");
    }

    private static String normalize(String value, String field) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized.toLowerCase(Locale.ROOT);
    }
}
