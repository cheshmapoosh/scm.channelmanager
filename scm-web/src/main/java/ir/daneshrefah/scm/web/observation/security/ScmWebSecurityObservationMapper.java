package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.uaa.starter.security.event.ScmSecurityEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ScmWebSecurityObservationMapper {
    private static final String AUTHENTICATION_SUCCESS = "authentication.success";
    private static final String AUTHENTICATION_FAILURE = "authentication.failure";
    private static final String ACCESS_DENIED = "access.denied";
    private static final String TOKEN_PREFIX = "token.";

    public ScmWebObservationEvent map(ScmSecurityEvent event) {
        String action = event == null ? "security.event" : event.eventType();
        Map<String, Object> attributes = attributes(event);
        return new ScmWebObservationEvent(
                "security",
                action,
                outcome(action),
                "SCM security event",
                attributes,
                attributes
        );
    }

    private Map<String, Object> attributes(ScmSecurityEvent event) {
        Map<String, Object> attributes = event == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(event.attributes()));
        if (event != null) {
            attributes.put("scm.event.type", event.eventType());
            attributes.put("scm.event.source", event.metadata().source());
            attributes.put("scm.event.occurred_at", event.occurredAt().toString());
        }
        return attributes;
    }

    private String outcome(String action) {
        if (AUTHENTICATION_SUCCESS.equals(action)) {
            return "success";
        }
        if (AUTHENTICATION_FAILURE.equals(action) || ACCESS_DENIED.equals(action) || action != null && action.startsWith(TOKEN_PREFIX)) {
            return "failure";
        }
        return "unknown";
    }
}
