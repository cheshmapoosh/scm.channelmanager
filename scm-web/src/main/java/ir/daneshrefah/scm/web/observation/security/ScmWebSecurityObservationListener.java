package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.uaa.client.security.event.ScmSecurityEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationTraceEventAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ConditionalOnBean(ScmObservation.class)
@Component
@Slf4j
public class ScmWebSecurityObservationListener {
    private final ScmObservation observation;
    private final ScmWebSecurityObservationMapper mapper;
    private final ScmWebObservationTraceEventAdapter traceEventAdapter;

    public ScmWebSecurityObservationListener(
            ScmObservation observation,
            ScmWebSecurityObservationMapper mapper,
            ScmWebObservationTraceEventAdapter traceEventAdapter
    ) {
        this.observation = observation;
        this.mapper = mapper;
        this.traceEventAdapter = traceEventAdapter;
    }

    @EventListener
    public void onSecurityEvent(ScmSecurityEvent event) {
        try {
            ScmWebObservationEvent mapped = mapper.map(event);
            observation.log()
                    .event()
                    .source(ScmWebSecurityObservationListener.class)
                    .loggerName(ScmWebSecurityObservationListener.class)
                    .message(mapped.message())
                    .category(mapped.category())
                    .action(mapped.action())
                    .outcome(mapped.outcome())
                    .attributes(mapped.logAttributes())
                    .write();
            traceEventAdapter.addSpanEventIfCurrent(ScmWebSecurityObservationListener.class, mapped);
        } catch (RuntimeException exception) {
            log.warn("event=SCM_WEB_SECURITY_OBSERVATION_FAILED outcome=ignored scmEventType={} failureType={} failureMessage={}",
                    event == null ? null : event.eventType(),
                    exception.getClass().getSimpleName(),
                    safeMessage(exception));
        }
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? null : exception.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
    }
}
