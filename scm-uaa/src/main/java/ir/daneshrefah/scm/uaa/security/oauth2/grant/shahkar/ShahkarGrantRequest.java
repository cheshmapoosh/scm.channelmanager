package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import org.springframework.security.core.Authentication;

import java.util.Set;

@SuppressWarnings("removal")
public record ShahkarGrantRequest(
        String nationalCode,
        String mobileNumber,
        String otpCode,
        String clientId,
        String accessParameter,
        String clientVersion,
        String clientSignature,
        Set<String> scopes,
        Authentication clientPrincipal,
        LegacyAppVersion appVersion
) {
}
