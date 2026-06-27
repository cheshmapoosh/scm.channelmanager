package ir.daneshrefah.scm.observation.metrics;

import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricTagSafetyTest {
    @Test
    void factoryRejectsHighCardinalityTags() {
        for (String name : new String[]{
                "correlation.id", "trace.id", "span.id", "username", "card.no", "account.no",
                "phone", "token", "otp", "message.sequence.id"
        }) {
            assertThrows(IllegalArgumentException.class,
                    () -> MetricTag.lowCardinality(name, "scm-host", "Forbidden tag."), name);
        }
    }

    @Test
    void rawMetricBuilderCannotBypassSafetyPolicy() {
        CapturingSink sink = new CapturingSink();
        new MetricObservationBuilder(sink, (name, value) -> value)
                .counter("scm.test")
                .tag("message.sequence.id", "sequence-1")
                .tag("operation_code", "balance")
                .increment();

        assertFalse(sink.tags.containsKey("message.sequence.id"));
    }

    private static final class CapturingSink implements MetricObservationSink {
        private final Map<String, String> tags = new LinkedHashMap<>();

        @Override
        public void incrementCounter(String name, Map<String, String> tags, double amount) {
            this.tags.putAll(tags);
        }

        @Override
        public void recordTimer(String name, Map<String, String> tags, long duration, TimeUnit unit) {
            this.tags.putAll(tags);
        }
    }
}
