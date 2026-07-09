package ir.daneshrefah.scm.web.observation.plugin;

import ir.daneshrefah.scm.common.event.plugin.ScmPluginEvent;
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
public class ScmWebPluginObservationListener {
    private final ScmObservation observation;
    private final ScmWebPluginObservationMapper mapper;
    private final ScmWebObservationTraceEventAdapter traceEventAdapter;

    public ScmWebPluginObservationListener(
            ScmObservation observation,
            ScmWebPluginObservationMapper mapper,
            ScmWebObservationTraceEventAdapter traceEventAdapter
    ) {
        this.observation = observation;
        this.mapper = mapper;
        this.traceEventAdapter = traceEventAdapter;
    }

    @EventListener
    public void onPluginEvent(ScmPluginEvent event) {
        try {
            ScmWebObservationEvent mapped = mapper.map(event);
            observation.log()
                    .event()
                    .source(ScmWebPluginObservationListener.class)
                    .loggerName(ScmWebPluginObservationListener.class)
                    .message(mapped.message())
                    .category(mapped.category())
                    .action(mapped.action())
                    .outcome(mapped.outcome())
                    .attributes(mapped.logAttributes())
                    .write();
            traceEventAdapter.addSpanEventIfCurrent(ScmWebPluginObservationListener.class, mapped);
        } catch (RuntimeException exception) {
            log.warn("event=SCM_WEB_PLUGIN_OBSERVATION_FAILED outcome=ignored scmEventType={} failureType={} failureMessage={}",
                    event == null ? null : event.eventType(),
                    exception.getClass().getSimpleName(),
                    safeMessage(exception));
        }
    }

    private String safeMessage(RuntimeException exception) {
        return exception.getMessage() == null ? null : exception.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
    }
}
