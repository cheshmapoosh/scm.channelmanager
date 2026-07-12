package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.core.integration.observability.CamelSecurityTraceEventRecorder;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.uaa.starter.security.event.ScmSecurityEvent;
import ir.daneshrefah.scm.uaa.starter.security.event.ScmSecurityEventType;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationTraceEventAdapter;
import org.apache.camel.Exchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConditionalOnBean(ScmObservation.class)
@Component
public class ScmWebCamelSecurityTraceEventRecorder implements CamelSecurityTraceEventRecorder {
    private final ScmWebSecurityObservationMapper mapper;
    private final ScmWebObservationTraceEventAdapter traceEventAdapter;

    public ScmWebCamelSecurityTraceEventRecorder(
            ScmWebSecurityObservationMapper mapper,
            ScmWebObservationTraceEventAdapter traceEventAdapter
    ) {
        this.mapper = mapper;
        this.traceEventAdapter = traceEventAdapter;
    }

    @Override
    public void record(
            Exchange exchange,
            String layer,
            ScmSecurityEventType eventType,
            Map<String, ?> attributes
    ) {
        if (exchange == null || eventType == null) {
            return;
        }
        ScmWebObservationEvent mapped = mapper.map(ScmSecurityEvent.of(eventType, attributes));
        traceEventAdapter.addSpanEvent(exchange, layer, mapped);
    }
}
