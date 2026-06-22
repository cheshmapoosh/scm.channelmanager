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
    private final ObservationDocumentFactory documentFactory;
    private final ObservationRecordValidator recordValidator;
    private final Clock clock;

    public ScmObservation(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSignalPolicy signalPolicy,
            ObservationEventDispatcher eventDispatcher,
            MetricObservationSink metricSink,
            TraceObservationSink traceSink,
            ObservationSanitizer sanitizer,
            ObservationDocumentFactory documentFactory,
            ObservationRecordValidator recordValidator,
            Clock clock
    ) {
        this.context = context;
        this.targetIndexResolver = targetIndexResolver;
        this.signalPolicy = signalPolicy;
        this.eventDispatcher = eventDispatcher;
        this.metricSink = metricSink;
        this.traceSink = traceSink;
        this.sanitizer = sanitizer;
        this.documentFactory = documentFactory;
        this.recordValidator = recordValidator;
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

    ObservationDocumentFactory documentFactory() {
        return documentFactory;
    }

    void validate(ObservationStream stream, ObservationRecordKind kind, boolean errorContext, Map<String, Object> document) {
        if (recordValidator != null) {
            recordValidator.validate(stream, kind, errorContext, document);
        }
    }

    private boolean eventEnabled(ObservationEventSignal signal) {
        return switch (signal) {
            case TRACE -> isEnabled(ObservationSignal.TRACE);
            case AUDIT -> isEnabled(ObservationSignal.AUDIT);
        };
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

}
