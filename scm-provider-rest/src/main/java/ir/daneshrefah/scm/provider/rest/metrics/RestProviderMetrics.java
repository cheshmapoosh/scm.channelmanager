package ir.daneshrefah.scm.provider.rest.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RestProviderMetrics {
    private final ConcurrentMap<String, CounterSet> counters = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public RestProviderMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.meterRegistry = meterRegistryProvider == null ? null : meterRegistryProvider.getIfAvailable();
    }

    public RestProviderMetrics() {
        this.meterRegistry = null;
    }

    public CounterSet provider(String provider) {
        return counters.computeIfAbsent(provider, ignored -> new CounterSet(provider, meterRegistry));
    }

    public static final class CounterSet {
        private final String provider;
        private final MeterRegistry meterRegistry;
        private final AtomicLong submitted = new AtomicLong();
        private final AtomicLong succeeded = new AtomicLong();
        private final AtomicLong failed = new AtomicLong();
        private final AtomicLong clientErrors = new AtomicLong();
        private final AtomicLong serverErrors = new AtomicLong();
        private final AtomicLong timedOut = new AtomicLong();
        private final AtomicLong rateLimited = new AtomicLong();
        private final AtomicLong rateLimitWaits = new AtomicLong();
        private final AtomicLong totalLatencyMs = new AtomicLong();
        private final AtomicLong tokenCacheHits = new AtomicLong();
        private final AtomicLong tokenCacheMisses = new AtomicLong();
        private final AtomicLong tokenCachePuts = new AtomicLong();
        private final AtomicLong tokenLockAcquired = new AtomicLong();
        private final AtomicLong tokenLockTimeouts = new AtomicLong();
        private final AtomicLong tokenRefreshes = new AtomicLong();
        private final AtomicLong tokenRefreshFailures = new AtomicLong();
        private final AtomicLong tokenRequestLatencyMs = new AtomicLong();
        private final AtomicLong customizerExecutions = new AtomicLong();
        private final AtomicLong customizerErrors = new AtomicLong();

        private CounterSet(String provider, MeterRegistry meterRegistry) {
            this.provider = provider;
            this.meterRegistry = meterRegistry;
        }

        public void submitted() {
            submitted.incrementAndGet();
        }

        public void succeeded() {
            succeeded.incrementAndGet();
        }

        public void failed() {
            failed.incrementAndGet();
        }

        public void clientError() {
            clientErrors.incrementAndGet();
        }

        public void serverError() {
            serverErrors.incrementAndGet();
        }

        public void timedOut() {
            timedOut.incrementAndGet();
        }

        public void rateLimited() {
            rateLimited.incrementAndGet();
        }

        public void rateLimitWait() {
            rateLimitWaits.incrementAndGet();
        }

        public void addLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                totalLatencyMs.addAndGet(elapsedMs);
            }
        }

        public void recordProviderRequestDuration(
                RestProviderResolvedConfig config,
                ProviderMessageCustomizerContext context,
                Duration duration,
                String outcome
        ) {
            if (meterRegistry == null || duration == null) {
                return;
            }
            Timer.builder("provider.request.duration")
                    .tags(tags(config, context, null, outcome))
                    .register(meterRegistry)
                    .record(duration.toNanos(), TimeUnit.NANOSECONDS);
        }

        public void recordProviderRequestError(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            increment("provider.request.error", tags(config, context, null, "error"));
        }

        public void tokenCacheHit() {
            tokenCacheHits.incrementAndGet();
        }

        public void tokenCacheHit(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenCacheHit();
            increment("provider.auth.cache.hit", tags(config, context, null, "hit"));
        }

        public void tokenCacheMiss() {
            tokenCacheMisses.incrementAndGet();
        }

        public void tokenCacheMiss(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenCacheMiss();
            increment("provider.auth.cache.miss", tags(config, context, null, "miss"));
        }

        public void tokenCachePut() {
            tokenCachePuts.incrementAndGet();
        }

        public void tokenCachePut(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenCachePut();
        }

        public void tokenLockAcquired() {
            tokenLockAcquired.incrementAndGet();
        }

        public void tokenLockAcquired(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenLockAcquired();
            increment("provider.auth.lock.acquired", tags(config, context, null, "acquired"));
        }

        public void tokenLockTimeout() {
            tokenLockTimeouts.incrementAndGet();
        }

        public void tokenLockTimeout(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenLockTimeout();
            increment("provider.auth.lock.timeout", tags(config, context, null, "timeout"));
        }

        public void tokenRefresh() {
            tokenRefreshes.incrementAndGet();
        }

        public void tokenRefresh(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenRefresh();
            increment("provider.auth.token.refresh", tags(config, context, null, "success"));
        }

        public void tokenRefreshFailure() {
            tokenRefreshFailures.incrementAndGet();
        }

        public void tokenRefreshFailure(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            tokenRefreshFailure();
            increment("provider.auth.token.refresh.error", tags(config, context, null, "error"));
        }

        public void addTokenRequestLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                tokenRequestLatencyMs.addAndGet(elapsedMs);
            }
        }

        public void recordTokenRequestDuration(
                RestProviderResolvedConfig config,
                ProviderMessageCustomizerContext context,
                Duration duration
        ) {
            if (duration != null) {
                addTokenRequestLatency(duration.toMillis());
            }
            if (meterRegistry == null || duration == null) {
                return;
            }
            Timer.builder("provider.auth.request.duration")
                    .tags(tags(config, context, null, "success"))
                    .register(meterRegistry)
                    .record(duration.toNanos(), TimeUnit.NANOSECONDS);
        }

        public void customizerExecution() {
            customizerExecutions.incrementAndGet();
        }

        public void customizerExecution(ProviderMessageCustomizerContext context, String customizerType, String phase) {
            customizerExecution();
            increment("provider.customizer.execution", tags(context, customizerType, phase));
        }

        public void customizerError() {
            customizerErrors.incrementAndGet();
        }

        public void customizerError(ProviderMessageCustomizerContext context, String customizerType, String phase) {
            customizerError();
            increment("provider.customizer.error", tags(context, customizerType, phase));
        }

        public long submittedCount() {
            return submitted.get();
        }

        public long succeededCount() {
            return succeeded.get();
        }

        public long failedCount() {
            return failed.get();
        }

        public long rateLimitedCount() {
            return rateLimited.get();
        }

        public long tokenCacheHitCount() {
            return tokenCacheHits.get();
        }

        public long tokenCacheMissCount() {
            return tokenCacheMisses.get();
        }

        public long tokenLockAcquiredCount() {
            return tokenLockAcquired.get();
        }

        public long tokenLockTimeoutCount() {
            return tokenLockTimeouts.get();
        }

        public long tokenRefreshCount() {
            return tokenRefreshes.get();
        }

        public long tokenRefreshFailureCount() {
            return tokenRefreshFailures.get();
        }

        public long customizerExecutionCount() {
            return customizerExecutions.get();
        }

        public long customizerErrorCount() {
            return customizerErrors.get();
        }

        private void increment(String name, Iterable<Tag> tags) {
            if (meterRegistry == null) {
                return;
            }
            Counter.builder(name).tags(tags).register(meterRegistry).increment();
        }

        private List<Tag> tags(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context, String customizerType, String outcome) {
            return List.of(
                    Tag.of("providerCode", value(config == null ? provider : config.provider())),
                    Tag.of("providerType", value(config == null ? "rest" : config.providerType())),
                    Tag.of("serviceCode", value(context == null ? "" : context.serviceCode())),
                    Tag.of("operationCode", value(context == null ? "" : context.operationCode())),
                    Tag.of("channelCode", value(context == null ? "" : context.channelCode())),
                    Tag.of("transportType", "rest"),
                    Tag.of("customizerType", value(customizerType)),
                    Tag.of("outcome", value(outcome))
            );
        }

        private List<Tag> tags(ProviderMessageCustomizerContext context, String customizerType, String phase) {
            return List.of(
                    Tag.of("providerCode", value(context == null ? provider : context.providerCode())),
                    Tag.of("providerType", value(context == null ? "" : context.providerType())),
                    Tag.of("serviceCode", value(context == null ? "" : context.serviceCode())),
                    Tag.of("operationCode", value(context == null ? "" : context.operationCode())),
                    Tag.of("channelCode", value(context == null ? "" : context.channelCode())),
                    Tag.of("transportType", value(context == null ? "" : context.transportType())),
                    Tag.of("customizerType", value(customizerType)),
                    Tag.of("outcome", value(phase))
            );
        }

        private String value(String value) {
            return value == null ? "" : value;
        }
    }
}
