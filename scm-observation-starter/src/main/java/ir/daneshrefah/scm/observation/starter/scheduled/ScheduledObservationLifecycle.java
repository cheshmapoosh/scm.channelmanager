package ir.daneshrefah.scm.observation.starter.scheduled;

import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;

/**
 * Starts a business trace at the moment a scheduled job actually executes.
 * Creating or registering a scheduler must not call this lifecycle.
 */
public class ScheduledObservationLifecycle {
    public static final String SCHEDULED_EXECUTE = "scheduled.execute";

    private final ScmObservation observation;

    public ScheduledObservationLifecycle(ScmObservation observation) {
        this.observation = observation;
    }

    public ObservationScope start(String jobName) {
        return start(jobName, null, null, null);
    }

    public ObservationScope start(String jobName, String correlationId) {
        return start(jobName, null, null, correlationId);
    }

    public ObservationScope start(
            String jobName,
            String triggerType,
            String routeId,
            String correlationId
    ) {
        String safeJobName = requiredLowCardinality(jobName, "Scheduled job name");
        return observation.trace()
                .source(ScheduledObservationLifecycle.class)
                .span(SCHEDULED_EXECUTE)
                .spanKind("internal")
                .action(SCHEDULED_EXECUTE)
                .outcome("success")
                .parentSpanId(null)
                .correlationId(textOrNull(correlationId, 128))
                .correlationType(CorrelationType.OPERATION.value())
                .attribute(CommonTraceAttributes.SCM_SCHEDULE_JOB_NAME, safeJobName)
                .attribute(CommonTraceAttributes.SCM_SCHEDULE_TRIGGER_TYPE,
                        textOrNull(triggerType, 64))
                .attribute(CommonTraceAttributes.SCM_ROUTE_ID, textOrNull(routeId, 256))
                .startDetached();
    }

    private String requiredLowCardinality(String value, String fieldDescription) {
        String normalized = textOrNull(value, 128);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldDescription + " is required");
        }
        return normalized;
    }

    private String textOrNull(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace('\r', ' ').replace('\n', ' ').trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }
}
