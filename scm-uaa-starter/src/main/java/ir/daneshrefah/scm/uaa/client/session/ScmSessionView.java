package ir.daneshrefah.scm.uaa.client.session;

import java.time.Duration;
import java.time.Instant;

public record ScmSessionView(
        String issuer,
        String sessionId,
        Instant issuedAt,
        Instant expiresAt,
        Duration maxIdle,
        String loginAuthenticationMethod,
        String loginAccessParameter
) {
}
