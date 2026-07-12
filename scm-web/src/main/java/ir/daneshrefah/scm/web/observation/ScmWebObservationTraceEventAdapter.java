package ir.daneshrefah.scm.web.observation;

import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import org.apache.camel.Exchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@ConditionalOnBean(ScmObservation.class)
@Component
public class ScmWebObservationTraceEventAdapter {
    private final CoreObservationTraceSupport observationTraceSupport;

    public ScmWebObservationTraceEventAdapter(CoreObservationTraceSupport observationTraceSupport) {
        this.observationTraceSupport = observationTraceSupport;
    }

    public void addSpanEvent(Exchange exchange, String layer, ScmWebObservationEvent event) {
        if (exchange == null || event == null) {
            return;
        }
        ObservationScope targetScope = observationTraceSupport.activeScope(exchange, layer);
        if (targetScope == null) {
            return;
        }
        Map<String, Object> attributes = new LinkedHashMap<>(event.traceAttributes());
        attributes.putIfAbsent("event.outcome", event.outcome());
        targetScope.event(event.action(), attributes);
    }
}
