package ir.daneshrefah.scm.uaa.security.oauth2.grant.smsotp;

import org.springframework.security.core.Authentication;

import java.util.Set;

public record SmsOtpAuthenticationRequest(
        String mobileNumber,
        String claimCode,
        Set<String> scopes,
        Authentication clientPrincipal,
        String clientId,
        String accessParameter,
        String clientVersion,
        String clientSignature,
        String activationCode
) {
}
