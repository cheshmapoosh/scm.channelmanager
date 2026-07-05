package ir.daneshrefah.scm.uaa.client.security.event;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmSecurityEventType implements ScmEventType {
    AUTHENTICATION_STARTED,
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAILURE,
    ACCESS_DENIED,
    TOKEN_MISSING,
    TOKEN_INVALID,
    TOKEN_EXPIRED;

    @Override
    public String code() {
        return name();
    }
}
