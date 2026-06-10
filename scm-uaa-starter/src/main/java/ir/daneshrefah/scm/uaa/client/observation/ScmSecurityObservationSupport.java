package ir.daneshrefah.scm.uaa.client.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.attributes.ScmMetricAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmOperationAttributes;

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
                .attribute(ScmOperationAttributes.NAME, operation)
                .start();
    }

    public void increment(
            String metricName,
            String operation,
            String outcome
    ) {
        observation.metric()
                .counter(metricName)
                .tag(ScmOperationAttributes.NAME, operation)
                .tag(ScmMetricAttributes.OUTCOME, outcome)
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
                .tag(ScmOperationAttributes.NAME, operation)
                .tag(ScmMetricAttributes.OUTCOME, outcome)
                .record(duration, unit);
    }
}
