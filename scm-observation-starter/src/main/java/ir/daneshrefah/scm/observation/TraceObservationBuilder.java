package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.trace.TraceObservationSpec;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;

public class TraceObservationBuilder extends AbstractObservationBuilder<TraceObservationBuilder> {
    private String spanName = "trace.span";
    private String spanKind = "internal";
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private boolean parentSpanIdProvided;
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

    public TraceObservationBuilder correlationType(String correlationType) {
        this.correlationType = correlationType;
        return this;
    }

    public ObservationScope start() {
        if (!observation.isEnabled(ObservationSignal.TRACE)) {
            return new ObservationScope(ir.daneshrefah.scm.observation.trace.TraceObservationHandle.NOOP);
        }
        TraceContext current = TraceContextHolder.current();
        TraceContext context = TraceContextHolder.childContext(current, traceId, spanId, correlationId, correlationType);
        String resolvedParentSpanId = parentSpanIdProvided ? textOrNull(parentSpanId) : currentSpanId(current);
        TraceContextHolder.Scope contextScope = TraceContextHolder.open(context);
        try {
            return observation.startTrace(new TraceObservationSpec(
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
                attributes
            ), contextScope);
        } catch (RuntimeException | Error ex) {
            contextScope.close();
            throw ex;
        }
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
}
