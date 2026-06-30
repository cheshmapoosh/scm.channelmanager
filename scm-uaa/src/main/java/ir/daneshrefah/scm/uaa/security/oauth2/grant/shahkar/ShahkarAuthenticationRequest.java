package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar;

public record ShahkarAuthenticationRequest(
        String maskedNationalCode,
        String maskedMobileNumber,
        boolean verify
) {
}
