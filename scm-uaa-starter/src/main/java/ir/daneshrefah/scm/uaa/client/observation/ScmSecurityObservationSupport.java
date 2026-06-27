package ir.daneshrefah.scm.uaa.client.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.uaa.client.observation.attributes.UaaClientMetricTags;
import ir.daneshrefah.scm.uaa.client.observation.attributes.UaaClientTraceAttributes;

import java.util.concurrent.TimeUnit;

public class ScmSecurityObservationSupport {
    private final ScmObservation observation;

    public ScmSecurityObservationSupport(ScmObservation observation) {
        this.observation = observation;
    }

    public ObservationScope startTrace(
            Class<?> source,
            String operation
    ) {
        return observation.trace()
                .source(source)
                .span(operation)
                .attribute(UaaClientTraceAttributes.OPERATION_NAME, operation)
                .start();
    }

    public void increment(
            String metricName,
            String operation,
            String outcome
    ) {
        observation.metric()
                .counter(metricName)
                .tag(UaaClientMetricTags.OPERATION_NAME, operation)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .increment();
    }

    public void recordDuration(
            String metricName,
            String operation,
            String outcome,
            long duration,
            TimeUnit unit
    ) {
        observation.metric()
                .timer(metricName)
                .tag(UaaClientMetricTags.OPERATION_NAME, operation)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(duration, unit);
    }
}
