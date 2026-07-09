package ir.daneshrefah.scm.web.observation.provider;

import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationTraceEventAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ConditionalOnBean(ScmObservation.class)
@Component
@Slf4j
public class ScmWebProviderObservationListener {
    private final ScmObservation observation;
    private final ScmWebProviderObservationMapper mapper;
    private final ScmWebObservationTraceEventAdapter traceEventAdapter;

    public ScmWebProviderObservationListener(
            ScmObservation observation,
            ScmWebProviderObservationMapper mapper,
            ScmWebObservationTraceEventAdapter traceEventAdapter
    ) {
        this.observation = observation;
        this.mapper = mapper;
        this.traceEventAdapter = traceEventAdapter;
    }

    @EventListener
    public void onProviderEvent(ScmProviderEvent event) {
        try {
            ScmWebObservationEvent mapped = mapper.map(event);
            observation.log()
                    .event()
                    .source(ScmWebProviderObservationListener.class)
                    .loggerName(ScmWebProviderObservationListener.class)
                    .message(mapped.message())
                    .category(mapped.category())
                    .action(mapped.action())
                    .outcome(mapped.outcome())
                    .attributes(mapped.logAttributes())
                    .write();
            traceEventAdapter.addSpanEventIfCurrent(ScmWebProviderObservationListener.class, mapped);
        } catch (RuntimeException exception) {
            log.warn("event=SCM_WEB_PROVIDER_OBSERVATION_FAILED outcome=ignored scmEventType={} failureType={} failureMessage={}",
                    event == null ? null : event.eventType(),
                    exception.getClass().getSimpleName(),
                    safeMessage(exception));
        }
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? null : exception.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
    }
}
