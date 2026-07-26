package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.core.integration.service.routing.DefaultRoutingDecisionPolicy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class TaskWorkflowPlanFingerprint {

    public String create(TaskWorkflowActionPlanConfig config) {
        StringBuilder normalized = new StringBuilder();
        append(normalized, normalize(config.serviceCode()));
        append(normalized, normalize(config.inboundAction()));
        append(normalized, normalize(config.actionPlanName()));
        append(normalized, normalize(config.definitionId()));
        append(normalized, config.routingStrategy().name());
        for (TaskWorkflowActionPlanStepConfig step : config.steps()) {
            append(normalized, String.valueOf(step.stepIndex()));
            append(normalized, normalize(step.stepId()));
            append(normalized, step.stepType().name());
            append(normalized, normalize(step.operationName()));
            append(normalized, normalize(StringUtils.defaultIfBlank(
                    step.decisionPolicy(),
                    DefaultRoutingDecisionPolicy.CODE
            )));
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void append(StringBuilder target, String value) {
        String normalized = value == null ? "" : value;
        target.append(normalized.length()).append(':').append(normalized).append(';');
    }

    private String normalize(String value) {
        String normalized = StringUtils.trimToEmpty(value);
        return normalized.toLowerCase(Locale.ROOT);
    }
}
