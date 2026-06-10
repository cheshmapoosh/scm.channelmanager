package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.metrics.MetricObservationBuilder;
import ir.daneshrefah.scm.observation.metrics.MetricObservationSink;
import ir.daneshrefah.scm.observation.metrics.NoopMetricObservationSink;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.trace.TraceObservationSink;
import ir.daneshrefah.scm.observation.trace.TraceObservationSpec;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScmObservation {
    private static final MetricObservationSink NOOP_METRIC_SINK = new NoopMetricObservationSink();

    private final ObservationContext context;
    private final ObsTargetIndexResolver targetIndexResolver;
    private final ObservationSignalPolicy signalPolicy;
    private final ObservationEventDispatcher eventDispatcher;
    private final MetricObservationSink metricSink;
    private final TraceObservationSink traceSink;
    private final ObservationSanitizer sanitizer;
    private final Clock clock;

    public ScmObservation(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSignalPolicy signalPolicy,
            ObservationEventDispatcher eventDispatcher,
            MetricObservationSink metricSink,
            TraceObservationSink traceSink,
            ObservationSanitizer sanitizer,
            Clock clock
    ) {
        this.context = context;
        this.targetIndexResolver = targetIndexResolver;
        this.signalPolicy = signalPolicy;
        this.eventDispatcher = eventDispatcher;
        this.metricSink = metricSink;
        this.traceSink = traceSink;
        this.sanitizer = sanitizer;
        this.clock = clock;
    }

    public LogObservationBuilder log() {
        return new LogObservationBuilder(this);
    }

    public TraceObservationBuilder trace() {
        return new TraceObservationBuilder(this);
    }

    public AuditObservationBuilder audit() {
        return new AuditObservationBuilder(this);
    }

    public MetricObservationBuilder metric() {
        MetricObservationSink selectedSink = isEnabled(ObservationSignal.METRIC) && metricSink != null ? metricSink : NOOP_METRIC_SINK;
        return new MetricObservationBuilder(selectedSink, sanitizer);
    }

    Instant now() {
        return Instant.now(clock);
    }

    void write(ObservationEventSignal signal, Class<?> sourceClass, Map<String, Object> document) {
        if (eventDispatcher != null && eventEnabled(signal)) {
            eventDispatcher.write(new ObservationEvent(signal, sourceClass, document));
        }
    }

    ObservationScope startTrace(TraceObservationSpec spec) {
        if (!isEnabled(ObservationSignal.TRACE) || traceSink == null) {
            return new ObservationScope(TraceObservationHandle.NOOP);
        }
        return new ObservationScope(traceSink.start(spec));
    }

    boolean isEnabled(ObservationSignal signal) {
        return signalPolicy != null && signalPolicy.isEnabled(signal);
    }

    private boolean eventEnabled(ObservationEventSignal signal) {
        return switch (signal) {
            case TRACE -> isEnabled(ObservationSignal.TRACE);
            case AUDIT -> isEnabled(ObservationSignal.AUDIT);
        };
    }

    LinkedHashMap<String, Object> baseDocument(
            ObservationStream stream,
            String eventKind,
            String eventCategory,
            String eventAction,
            String eventOutcome,
            String correlationId,
            boolean legacyEnabled,
            String legacyTable,
            Instant timestamp
    ) {
        if (legacyEnabled) {
            validateLegacyTable(legacyTable);
        }
        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put("@timestamp", timestamp.toString());
        document.put("event.stream", stream.value());
        document.put("event.kind", textOrDefault(eventKind, "event"));
        document.put("event.category", textOrDefault(eventCategory, stream.value()));
        document.put("event.action", textOrDefault(eventAction, stream.value() + ".event"));
        document.put("event.outcome", textOrDefault(eventOutcome, "unknown"));
        document.put("scm.target.index", targetIndexResolver.resolve(stream, context, timestamp));
        document.put("scm.target.legacy.enabled", legacyEnabled);
        if (legacyEnabled) {
            document.put("scm.target.legacy.table", legacyTable.trim());
        }
        document.put("scm.platform", context.platform());
        document.put("service.name", context.appName());
        document.put("deployment.environment", context.appProfile());
        document.put("scm.app.name", context.appName());
        document.put("scm.app.profile", context.appProfile());
        document.put("scm.app.label", context.appLabel());
        document.put("scm.gateway.name", context.gatewayName());
        document.put("scm.channel.code", context.channelCode());
        document.put("scm.correlation_id", textOrDefault(correlationId, ObservationIds.correlationId()));
        return document;
    }

    void putAttribute(Map<String, Object> document, String fieldName, Object value) {
        if (fieldName == null || fieldName.isBlank() || value == null) {
            return;
        }
        Object sanitized = sanitizer.sanitize(fieldName.trim(), value);
        if (sanitized != null) {
            document.put(fieldName.trim(), sanitized);
        }
    }

    <V> void putAttribute(Map<String, Object> document, ObservationAttributeKey<V> key, V value) {
        if (key != null) {
            putAttribute(document, key.name(), value);
        }
    }

    void putAttributes(Map<String, Object> document, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            putAttribute(document, entry.getKey(), entry.getValue());
        }
    }

    private void validateLegacyTable(String legacyTable) {
        ObservationLegacyTables.validate(legacyTable);
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
