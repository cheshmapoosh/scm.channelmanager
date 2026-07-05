package ir.daneshrefah.scm.cmconnector.session.model;

public record CmSessionResponse(
        String issuer,
        // Required by the legacy CM compatibility contract. Do not log, trace, or metric-tag this value.
        String sessionId,
        Long issuedAt,
        Long expiresAt,
        Integer maxIdle,
        String loginAuthenticationMethod,
        // Required by the legacy CM compatibility contract. Do not log, trace, or metric-tag this value.
        String loginAccessParameter
) {
}
