package ir.daneshrefah.scm.observation.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MicrometerMetricObservationSink implements MetricObservationSink {
    private final MeterRegistry meterRegistry;

    public MicrometerMetricObservationSink(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void incrementCounter(String name, Map<String, String> tags, double amount) {
        if (name == null || name.isBlank()) {
            return;
        }
        Counter.builder(name)
                .tags(tags(tags))
                .register(meterRegistry)
                .increment(amount);
    }

    @Override
    public void recordTimer(String name, Map<String, String> tags, long duration, TimeUnit unit) {
        if (name == null || name.isBlank() || unit == null) {
            return;
        }
        Timer.builder(name)
                .tags(tags(tags))
                .register(meterRegistry)
                .record(duration, unit);
    }

    private Tags tags(Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Tags.empty();
        }
        List<Tag> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            result.add(Tag.of(entry.getKey(), entry.getValue()));
        }
        return Tags.of(result);
    }
}
