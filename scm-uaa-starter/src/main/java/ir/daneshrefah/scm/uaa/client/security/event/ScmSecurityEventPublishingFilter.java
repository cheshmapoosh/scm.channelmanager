package ir.daneshrefah.scm.uaa.client.security.event;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

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
        publish(ScmSecurityEventType.AUTHENTICATION_STARTED, request);
        filterChain.doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isRealAuthenticatedPrincipal(authentication)) {
            publish(ScmSecurityEventType.AUTHENTICATION_SUCCESS, request);
        }
    }

    private void publish(ScmSecurityEventType type, HttpServletRequest request) {
        if (eventPublisher == null) {
            return;
        }
        Map<String, Object> attributes = ScmSecurityEventAttributes.base(request, properties);
        eventPublisher.publish(ScmSecurityEvent.of(type, attributes));
    }

    private boolean isRealAuthenticatedPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String className = authentication.getClass().getName();
        if (className != null && className.contains("AnonymousAuthenticationToken")) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        return principal != null && !"anonymousUser".equals(String.valueOf(principal));
    }
}
