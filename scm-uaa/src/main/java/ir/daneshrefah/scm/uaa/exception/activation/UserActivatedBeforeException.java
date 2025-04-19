package ir.daneshrefah.scm.uaa.exception.activation;

import ir.daneshrefah.scm.uaa.exception.BaseAuthenticationException;

public class UserActivatedBeforeException extends BaseAuthenticationException {

    public UserActivatedBeforeException() {
        super("user activated before.", null);
    }

    @Override
    public String getErrorCode() {
        return "user_does_not_need_activation";
    }
}
