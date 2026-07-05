package ir.daneshrefah.scm.web.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@ConditionalOnBean(ScmObservation.class)
@Component
public class ScmWebObservationTraceEventAdapter {
    public void addSpanEventIfCurrent(Class<?> source, ScmWebObservationEvent event) {
        ObservationScope currentScope = ObservationScope.current();
        if (currentScope == null || event == null) {
            return;
        }
        currentScope.event(event.action(), event.traceAttributes());
    }
}
