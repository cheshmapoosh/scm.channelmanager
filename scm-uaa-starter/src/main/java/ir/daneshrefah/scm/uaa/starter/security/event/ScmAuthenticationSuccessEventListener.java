package ir.daneshrefah.scm.uaa.starter.security.event;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import java.util.Map;

@Slf4j
public class ScmAuthenticationSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private final ScmEventPublisher eventPublisher;

    public ScmAuthenticationSuccessEventListener(ScmEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        if (eventPublisher == null || event == null || !isRealAuthenticatedPrincipal(event.getAuthentication())) {
            return;
        }
        Map<String, Object> attributes = ScmSecurityEventAttributes.authentication(event.getAuthentication());
        safePublish(ScmSecurityEvent.of(ScmSecurityEventType.AUTHENTICATION_SUCCESS, attributes));
    }

    private boolean isRealAuthenticatedPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        return principal != null && !"anonymousUser".equals(String.valueOf(principal));
    }

    private void safePublish(ScmSecurityEvent event) {
        try {
            eventPublisher.publish(event);
        } catch (RuntimeException ex) {
            log.warn("event=SCM_SECURITY_EVENT_PUBLISH_FAILED outcome=ignored failureType={} failureMessage={}",
                    ex.getClass().getSimpleName(),
                    ScmSecurityEventAttributes.safeMessage(ex));
        }
    }
}
