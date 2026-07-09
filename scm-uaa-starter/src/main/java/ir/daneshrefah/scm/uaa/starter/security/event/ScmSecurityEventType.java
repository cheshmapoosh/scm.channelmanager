package ir.daneshrefah.scm.uaa.starter.security.event;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmSecurityEventType implements ScmEventType {
    AUTHENTICATION_STARTED("authentication.started"),
    AUTHENTICATION_SUCCESS("authentication.success"),
    AUTHENTICATION_FAILURE("authentication.failure"),
    ACCESS_DENIED("access.denied"),
    TOKEN_MISSING("token.missing"),
    TOKEN_INVALID("token.invalid"),
    TOKEN_EXPIRED("token.expired");

    private final String code;

    ScmSecurityEventType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
