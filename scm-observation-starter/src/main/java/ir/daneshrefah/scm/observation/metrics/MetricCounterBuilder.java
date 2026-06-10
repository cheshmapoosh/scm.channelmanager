package ir.daneshrefah.scm.observation.metrics;

import ir.daneshrefah.scm.observation.ObservationSanitizer;

public class MetricCounterBuilder extends AbstractMetricBuilder<MetricCounterBuilder> {
    MetricCounterBuilder(MetricObservationSink sink, ObservationSanitizer sanitizer, String name) {
        super(sink, sanitizer, name);
    }

    public void increment() {
        increment(1.0D);
    }

    public void increment(double amount) {
        if (amount > 0.0D) {
            sink.incrementCounter(name, tags(), amount);
        }
    }

    @Override
    protected MetricCounterBuilder self() {
        return this;
    }
}
