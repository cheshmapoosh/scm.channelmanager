package ir.daneshrefah.scm.uaa.exception.activation;

import ir.daneshrefah.scm.uaa.exception.BaseAuthenticationException;

public class InvalidActivationTerminalCodeException extends BaseAuthenticationException {

    public InvalidActivationTerminalCodeException() {
        super("activation terminal code is invalid.", null);
    }

    @Override
    public String getErrorCode() {
        return "invalid_activator_terminal";
    }
}
