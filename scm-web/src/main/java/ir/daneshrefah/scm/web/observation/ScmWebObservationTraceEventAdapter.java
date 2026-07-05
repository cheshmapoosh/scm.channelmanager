package ir.daneshrefah.scm.web.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.TraceContext;
import ir.daneshrefah.scm.observation.TraceContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ScmWebObservationTraceEventAdapter {
    private final ScmObservation observation;

    public ScmWebObservationTraceEventAdapter(ScmObservation observation) {
        this.observation = observation;
    }

    public void addSpanEventIfCurrent(Class<?> source, ScmWebObservationEvent event) {
        TraceContext current = TraceContextHolder.current();
        if (current == null || current.traceId() == null || current.spanId() == null || event == null) {
            return;
        }
        try (ObservationScope scope = observation.trace()
                .source(source)
                .span(event.action())
                .spanKind("internal")
                .action(event.action())
                .outcome(event.outcome())
                .correlationId(current.correlationId())
                .attribute("event.category", event.category())
                .attributes(event.traceAttributes())
                .start()) {
            scope.outcome(event.outcome());
        }
    }
}
