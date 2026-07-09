package ir.daneshrefah.scm.uaa.starter.security.event;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.uaa.starter.properties.ScmResourceServerProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class ScmSecurityEventPublishingFilter extends OncePerRequestFilter {
    private final ScmEventPublisher eventPublisher;
    private final ScmResourceServerProperties properties;

    public ScmSecurityEventPublishingFilter(
            ScmEventPublisher eventPublisher,
            ScmResourceServerProperties properties
    ) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (shouldPublishAuthenticationStarted(request)) {
            publish(ScmSecurityEventType.AUTHENTICATION_STARTED, request);
        }
        filterChain.doFilter(request, response);
    }

    private void publish(ScmSecurityEventType type, HttpServletRequest request) {
        if (eventPublisher == null) {
            return;
        }
        Map<String, Object> attributes = ScmSecurityEventAttributes.base(request, properties);
        safePublish(ScmSecurityEvent.of(type, attributes));
    }

    private boolean shouldPublishAuthenticationStarted(HttpServletRequest request) {
        return !ScmSecurityEventAttributes.isPublicEndpoint(request, properties)
                && ScmSecurityEventAttributes.hasBearerCredential(request, properties);
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
