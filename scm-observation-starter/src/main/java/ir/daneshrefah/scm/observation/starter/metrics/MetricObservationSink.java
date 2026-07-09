package ir.daneshrefah.scm.observation.starter.metrics;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface MetricObservationSink {
    void incrementCounter(String name, Map<String, String> tags, double amount);

    void recordTimer(String name, Map<String, String> tags, long duration, TimeUnit unit);
}
