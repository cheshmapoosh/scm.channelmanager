package ir.daneshrefah.scm.cache.client.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.cache.client.observation.attributes.CacheClientMetricTags;
import ir.daneshrefah.scm.cache.client.observation.attributes.CacheClientTraceAttributes;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;

import java.util.concurrent.TimeUnit;

public class CacheClientObservationSupport {
    private final ScmObservation observation;

    public CacheClientObservationSupport(ScmObservation observation) {
        this.observation = observation;
    }

    public ObservationScope startTrace(
            Class<?> source,
            String operation
    ) {
        return observation.trace()
                .source(source)
                .span(operation)
                .attribute(CacheClientTraceAttributes.OPERATION_NAME, operation)
                .start();
    }

    public void increment(
            String metricName,
            String operation,
            String outcome
    ) {
        observation.metric()
                .counter(metricName)
                .tag(CacheClientMetricTags.OPERATION_NAME, operation)
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
                .tag(CacheClientMetricTags.OPERATION_NAME, operation)
                .tag(CommonMetricTags.OUTCOME, outcome)
                .record(duration, unit);
    }
}
