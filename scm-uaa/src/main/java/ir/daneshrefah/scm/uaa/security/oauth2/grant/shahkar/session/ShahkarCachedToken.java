package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.session;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;

import java.io.Serializable;

public record ShahkarCachedToken(
        ShahkarGrantAuthenticationToken token,
        long expiresAtEpochMillis
) implements Serializable {
}
