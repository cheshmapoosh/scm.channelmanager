package ir.daneshrefah.scm.observation.starter.metrics;

import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;

public class MetricObservationBuilder {
    private final MetricObservationSink sink;
    private final ObservationSanitizer sanitizer;

    public MetricObservationBuilder(MetricObservationSink sink, ObservationSanitizer sanitizer) {
        this.sink = sink;
        this.sanitizer = sanitizer;
    }

    public MetricCounterBuilder counter(String name) {
        return new MetricCounterBuilder(sink, sanitizer, name);
    }

    public MetricTimerBuilder timer(String name) {
        return new MetricTimerBuilder(sink, sanitizer, name);
    }
}
