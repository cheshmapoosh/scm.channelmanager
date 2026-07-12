package ir.daneshrefah.scm.observation.starter;

import ir.daneshrefah.scm.observation.starter.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationSpec;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;

public class TraceObservationBuilder extends AbstractObservationBuilder<TraceObservationBuilder> {
    private String spanName = "trace.span";
    private String spanKind = "internal";
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private boolean parentSpanIdProvided;
    private String traceFlags;
    private String correlationType;

    TraceObservationBuilder(ScmObservation observation) {
        super(observation);
        this.action = spanName;
    }

    public TraceObservationBuilder span(String spanName) {
        this.spanName = spanName;
        this.action = spanName;
        return this;
    }

    public TraceObservationBuilder spanKind(String spanKind) {
        this.spanKind = spanKind;
        return this;
    }

    public TraceObservationBuilder traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public TraceObservationBuilder spanId(String spanId) {
        this.spanId = spanId;
        return this;
    }

    public TraceObservationBuilder parentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
        this.parentSpanIdProvided = true;
        return this;
    }

    public TraceObservationBuilder traceFlags(String traceFlags) {
        this.traceFlags = traceFlags;
        return this;
    }

    public TraceObservationBuilder correlationType(String correlationType) {
        this.correlationType = correlationType;
        return this;
    }

    public ObservationScope start() {
        return startThreadBound();
    }

    /**
     * Starts a span whose lifecycle is owned explicitly by its caller rather than by the current thread.
     * Detached spans are suitable for asynchronous processing frameworks such as Camel: this method does
     * not install {@link ObservationScope#current()} state or a {@link TraceContextHolder} binding.
     */
    public ObservationScope startDetached() {
        TraceContext context = new TraceContext(
                firstText(traceId, ObservationIds.traceId()),
                firstText(spanId, ObservationIds.spanId()),
                firstText(correlationId, ObservationIds.correlationId()),
                firstText(correlationType, CorrelationType.OPERATION.value()),
                firstText(traceFlags, TraceFlags.DEFAULT)
        );
        String resolvedParentSpanId = parentSpanIdProvided ? textOrNull(parentSpanId) : null;
        TraceObservationSpec spec = spec(context, resolvedParentSpanId);
        if (!observation.isEnabled(ObservationSignal.TRACE)) {
            return ObservationScope.detached(TraceObservationHandle.NOOP);
        }
        return observation.startDetachedTrace(spec);
    }

    private ObservationScope startThreadBound() {
        if (!observation.isEnabled(ObservationSignal.TRACE)) {
            return new ObservationScope(TraceObservationHandle.NOOP);
        }
        TraceContext current = TraceContextHolder.current();
        TraceContext context = TraceContextHolder.childContext(current, traceId, spanId, correlationId, correlationType, traceFlags);
        String resolvedParentSpanId = parentSpanIdProvided ? textOrNull(parentSpanId) : currentSpanId(current);
        TraceContextHolder.Scope contextScope = TraceContextHolder.open(context);
        try {
            return observation.startTrace(spec(context, resolvedParentSpanId), contextScope);
        } catch (RuntimeException | Error ex) {
            contextScope.close();
            throw ex;
        }
    }

    private TraceObservationSpec spec(TraceContext context, String resolvedParentSpanId) {
        return new TraceObservationSpec(
                sourceClass,
                spanName,
                spanKind,
                action,
                outcome,
                context.correlationId(),
                context.correlationType(),
                context.traceId(),
                context.spanId(),
                resolvedParentSpanId,
                context.traceFlags(),
                attributes
        );
    }

    @Override
    protected TraceObservationBuilder self() {
        return this;
    }

    private String currentSpanId(TraceContext current) {
        return current == null ? null : textOrNull(current.spanId());
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstText(String first, String fallback) {
        String value = textOrNull(first);
        return value == null ? fallback : value;
    }
}
