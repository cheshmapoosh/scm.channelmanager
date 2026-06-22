package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.trace.TraceObservationSpec;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;

public class TraceObservationBuilder extends AbstractObservationBuilder<TraceObservationBuilder> {
    private String spanName = "trace.span";
    private String spanKind = "internal";
    private String traceId;
    private String spanId;
    private String parentSpanId = "";

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
        return this;
    }

    public ObservationScope start() {
        if (!observation.isEnabled(ObservationSignal.TRACE)) {
            return new ObservationScope(ir.daneshrefah.scm.observation.trace.TraceObservationHandle.NOOP);
        }
        return observation.startTrace(new TraceObservationSpec(
                sourceClass,
                spanName,
                spanKind,
                action,
                outcome,
                correlationId,
                traceId,
                spanId,
                parentSpanId,
                attributes
        ));
    }

    @Override
    protected TraceObservationBuilder self() {
        return this;
    }
}
