package ir.daneshrefah.scm.observation.starter.metrics;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NoopMetricObservationSink implements MetricObservationSink {
    @Override
    public void incrementCounter(String name, Map<String, String> tags, double amount) {
        // Intentionally no-op when no MeterRegistry is available.
    }

    @Override
    public void recordTimer(String name, Map<String, String> tags, long duration, TimeUnit unit) {
        // Intentionally no-op when no MeterRegistry is available.
    }
}
