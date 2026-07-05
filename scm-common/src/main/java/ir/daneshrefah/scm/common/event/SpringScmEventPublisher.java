package ir.daneshrefah.scm.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class SpringScmEventPublisher implements ScmEventPublisher {
    private final ApplicationEventPublisher publisher;

    public SpringScmEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(ScmEvent event) {
        if (event == null || publisher == null) {
            return;
        }
        try {
            publisher.publishEvent(event);
        } catch (RuntimeException exception) {
            log.warn("event=SCM_EVENT_PUBLISH_FAILED scmEventType={} outcome=ignored failureType={} failureMessage={}",
                    event.eventType(),
                    exception.getClass().getSimpleName(),
                    safeMessage(exception));
        }
    }

    private String safeMessage(RuntimeException exception) {
        if (exception.getMessage() == null) {
            return null;
        }
        return exception.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
    }
}
