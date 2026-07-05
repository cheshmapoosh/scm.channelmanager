package ir.daneshrefah.scm.uaa.client.security.event;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.Map;

public class ScmPublishingAccessDeniedHandler implements AccessDeniedHandler {
    private final AccessDeniedHandler delegate = new BearerTokenAccessDeniedHandler();
    private final ScmEventPublisher eventPublisher;
    private final ScmResourceServerProperties properties;

    public ScmPublishingAccessDeniedHandler(
            ScmEventPublisher eventPublisher,
            ScmResourceServerProperties properties
    ) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        publish(request, accessDeniedException);
        delegate.handle(request, response, accessDeniedException);
    }

    private void publish(HttpServletRequest request, AccessDeniedException exception) {
        if (eventPublisher == null) {
            return;
        }
        Map<String, Object> attributes = ScmSecurityEventAttributes.base(request, properties);
        ScmSecurityEventAttributes.put(attributes, "security.failure.reason", ScmSecurityEventType.ACCESS_DENIED.code());
        ScmSecurityEventAttributes.put(attributes, "error.code", "access_denied");
        ScmSecurityEventAttributes.put(attributes, "error.message", ScmSecurityEventAttributes.safeMessage(exception));
        eventPublisher.publish(ScmSecurityEvent.of(ScmSecurityEventType.ACCESS_DENIED, attributes));
    }
}
