package ir.daneshrefah.scm.provider.shetab.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ShetabProviderMetrics {
    private final ConcurrentMap<String, CounterSet> counters = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public ShetabProviderMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.meterRegistry = meterRegistryProvider == null ? null : meterRegistryProvider.getIfAvailable();
    }

    public ShetabProviderMetrics() {
        this.meterRegistry = null;
    }

    public CounterSet provider(String provider) {
        return counters.computeIfAbsent(provider, ignored -> new CounterSet(provider, meterRegistry));
    }

    public static final class CounterSet {
        private final String provider;
        private final MeterRegistry meterRegistry;
        private final AtomicLong submitted = new AtomicLong();
        private final AtomicLong rateLimited = new AtomicLong();
        private final AtomicLong rateLimitWaits = new AtomicLong();
        private final AtomicLong queueRejected = new AtomicLong();
        private final AtomicLong succeeded = new AtomicLong();
        private final AtomicLong sent = new AtomicLong();
        private final AtomicLong received = new AtomicLong();
        private final AtomicLong failed = new AtomicLong();
        private final AtomicLong timedOut = new AtomicLong();
        private final AtomicLong totalLatencyMs = new AtomicLong();
        private final AtomicLong customizerExecutions = new AtomicLong();
        private final AtomicLong customizerErrors = new AtomicLong();

        private CounterSet(String provider, MeterRegistry meterRegistry) {
            this.provider = provider;
            this.meterRegistry = meterRegistry;
        }

        public void submitted() {
            submitted.incrementAndGet();
        }

        public void rateLimited() {
            rateLimited.incrementAndGet();
        }

        public void rateLimitWait() {
            rateLimitWaits.incrementAndGet();
        }

        public void queueRejected() {
            queueRejected.incrementAndGet();
        }

        public void succeeded() {
            succeeded.incrementAndGet();
        }

        public void sent() {
            sent.incrementAndGet();
        }

        public void received() {
            received.incrementAndGet();
        }

        public void failed() {
            failed.incrementAndGet();
        }

        public void timedOut() {
            timedOut.incrementAndGet();
        }

        public void addLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                totalLatencyMs.addAndGet(elapsedMs);
            }
        }

        public void recordProviderRequestDuration(
                ShetabResolvedConfig config,
                ProviderMessageCustomizerContext context,
                Duration duration,
                String outcome
        ) {
            recordProviderRequestDuration(config, context, duration, null, outcome);
        }

        public void recordProviderRequestDuration(
                ShetabResolvedConfig config,
                ProviderMessageCustomizerContext context,
                Duration duration,
                ProviderBusinessOutcome outcome
        ) {
            recordProviderRequestDuration(
                    config,
                    context,
                    duration,
                    outcome == null ? null : outcome.errorCode(),
                    outcome == null ? null : outcome.eventOutcome()
            );
        }

        private void recordProviderRequestDuration(
                ShetabResolvedConfig config,
                ProviderMessageCustomizerContext context,
                Duration duration,
                String errorCode,
                String outcome
        ) {
            if (meterRegistry == null || duration == null) {
                return;
            }
            Timer.builder("provider.request.duration")
                    .tags(tags(config, context, null, errorCode, outcome))
                    .register(meterRegistry)
                    .record(duration.toNanos(), TimeUnit.NANOSECONDS);
        }

        public void recordProviderRequestError(ShetabResolvedConfig config, ProviderMessageCustomizerContext context) {
            increment("provider.request.error", tags(config, context, null, null, "error"));
        }

        public void recordProviderRequestError(
                ShetabResolvedConfig config,
                ProviderMessageCustomizerContext context,
                ProviderBusinessOutcome outcome
        ) {
            increment("provider.request.error", tags(
                    config,
                    context,
                    null,
                    outcome == null ? null : outcome.errorCode(),
                    outcome == null ? "failure" : outcome.eventOutcome()
            ));
        }

        public void customizerExecution(ProviderMessageCustomizerContext context, String customizerType, String phase) {
            customizerExecutions.incrementAndGet();
            increment("provider.customizer.execution", tags(context, customizerType, phase));
        }

        public void customizerError(ProviderMessageCustomizerContext context, String customizerType, String phase) {
            customizerErrors.incrementAndGet();
            increment("provider.customizer.error", tags(context, customizerType, phase));
        }

        public long submittedCount() {
            return submitted.get();
        }

        public long rateLimitedCount() {
            return rateLimited.get();
        }

        public long sentCount() {
            return sent.get();
        }

        public long receivedCount() {
            return received.get();
        }

        public long failedCount() {
            return failed.get();
        }

        public long succeededCount() {
            return succeeded.get();
        }

        private void increment(String name, Iterable<Tag> tags) {
            if (meterRegistry == null) {
                return;
            }
            Counter.builder(name).tags(tags).register(meterRegistry).increment();
        }

        private List<Tag> tags(
                ShetabResolvedConfig config,
                ProviderMessageCustomizerContext context,
                String customizerType,
                String errorCode,
                String outcome
        ) {
            return List.of(
                    Tag.of("providerCode", value(config == null ? provider : config.provider())),
                    Tag.of("scheme", value(config == null ? "" : config.scheme())),
                    Tag.of("serviceCode", value(context == null ? "" : context.serviceCode())),
                    Tag.of("operationCode", value(context == null ? "" : context.operationCode())),
                    Tag.of("channelCode", value(context == null ? "" : context.channelCode())),
                    Tag.of("customizerType", value(customizerType)),
                    Tag.of("errorCode", value(errorCode)),
                    Tag.of("outcome", value(outcome))
            );
        }

        private List<Tag> tags(ProviderMessageCustomizerContext context, String customizerType, String phase) {
            return List.of(
                    Tag.of("providerCode", value(context == null ? provider : context.providerCode())),
                    Tag.of("scheme", value(context == null ? "" : context.scheme())),
                    Tag.of("serviceCode", value(context == null ? "" : context.serviceCode())),
                    Tag.of("operationCode", value(context == null ? "" : context.operationCode())),
                    Tag.of("channelCode", value(context == null ? "" : context.channelCode())),
                    Tag.of("customizerType", value(customizerType)),
                    Tag.of("outcome", value(phase))
            );
        }

        private String value(String value) {
            return value == null ? "" : value;
        }
    }
}
