package ir.daneshrefah.scm.cmconnector.session.model;

public record CmSessionResponse(
        String issuer,
        String sessionId,
        Long issuedAt,
        Long expiresAt,
        Integer maxIdle,
        String loginAuthenticationMethod,
        String loginAccessParameter
) {
}
