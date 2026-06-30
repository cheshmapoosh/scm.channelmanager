package ir.daneshrefah.scm.uaa.exception.activation.nib;

import ir.daneshrefah.scm.common.exception.ScmException;

public class NibActivationAlreadyExistsException extends ScmException {
    public NibActivationAlreadyExistsException() {
        super("nib_activation_already_exists", "NIB activation already exists");
    }

    public NibActivationAlreadyExistsException(Throwable cause) {
        super("nib_activation_already_exists", "NIB activation already exists", cause);
    }
}
