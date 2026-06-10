package ir.daneshrefah.scm.observation.metrics;

import ir.daneshrefah.scm.observation.ObservationSanitizer;

import java.util.concurrent.TimeUnit;

public class MetricTimerBuilder extends AbstractMetricBuilder<MetricTimerBuilder> {
    MetricTimerBuilder(MetricObservationSink sink, ObservationSanitizer sanitizer, String name) {
        super(sink, sanitizer, name);
    }

    public void record(long duration, TimeUnit unit) {
        if (duration >= 0L) {
            sink.recordTimer(name, tags(), duration, unit);
        }
    }

    @Override
    protected MetricTimerBuilder self() {
        return this;
    }
}
