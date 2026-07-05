package ir.daneshrefah.scm.uaa.client.session;

import java.time.Duration;
import java.time.Instant;

public record ScmSessionView(
        String issuer,
        // Legacy CM compatibility field. Keep in response mapping, but never log, trace, or metric-tag this value.
        String sessionId,
        Instant issuedAt,
        Instant expiresAt,
        Duration maxIdle,
        String loginAuthenticationMethod,
        // Legacy CM compatibility field. Keep in response mapping, but never log, trace, or metric-tag this value.
        String loginAccessParameter
) {
}
