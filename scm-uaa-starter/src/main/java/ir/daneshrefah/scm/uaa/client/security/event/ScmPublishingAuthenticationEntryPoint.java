package ir.daneshrefah.scm.uaa.client.security.event;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;

public class ScmPublishingAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ScmEventPublisher eventPublisher;
    private final ScmResourceServerProperties properties;

    public ScmPublishingAuthenticationEntryPoint(
            ScmEventPublisher eventPublisher,
            ScmResourceServerProperties properties
    ) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        publish(request, authException);
        delegate.commence(request, response, authException);
    }

    private void publish(HttpServletRequest request, AuthenticationException exception) {
        if (eventPublisher == null) {
            return;
        }
        ScmSecurityEventType tokenEventType = ScmSecurityEventAttributes.failureType(request, properties, exception);
        Map<String, Object> attributes = ScmSecurityEventAttributes.base(request, properties);
        ScmSecurityEventAttributes.put(attributes, "security.failure.reason", tokenEventType.code());
        ScmSecurityEventAttributes.put(attributes, "error.code", ScmSecurityEventAttributes.errorCode(exception));
        ScmSecurityEventAttributes.put(attributes, "error.message", ScmSecurityEventAttributes.safeMessage(exception));
        eventPublisher.publish(ScmSecurityEvent.of(tokenEventType, attributes));
        eventPublisher.publish(ScmSecurityEvent.of(ScmSecurityEventType.AUTHENTICATION_FAILURE, attributes));
    }
}
