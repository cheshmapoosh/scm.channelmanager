package ir.daneshrefah.scm.web.observation.security;

import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Set;

public interface GatewayAuthenticationTraceContributor {
    boolean supports(Authentication authentication);

    Map<String, Object> attributes(Authentication authentication);

    default Map<String, Object> authenticatedAttributes(Authentication authentication, Set<String> requestedNames) {
        return Map.of();
    }
}
