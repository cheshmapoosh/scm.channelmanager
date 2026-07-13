package ir.daneshrefah.scm.web.observation.security;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface GatewayAuthenticationTraceContributor {
    boolean supports(Authentication authentication);

    Map<String, Object> attributes(Authentication authentication);
}
