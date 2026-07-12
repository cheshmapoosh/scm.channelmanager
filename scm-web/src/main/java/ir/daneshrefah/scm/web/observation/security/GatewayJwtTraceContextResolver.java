package ir.daneshrefah.scm.web.observation.security;

import org.springframework.security.core.Authentication;

public interface GatewayJwtTraceContextResolver {
    GatewayJwtTraceContext resolve(Authentication authentication);
}
