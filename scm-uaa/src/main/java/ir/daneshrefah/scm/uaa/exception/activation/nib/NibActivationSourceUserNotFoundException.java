package ir.daneshrefah.scm.uaa.exception.activation.nib;

import ir.daneshrefah.scm.common.exception.ScmException;

public class NibActivationSourceUserNotFoundException extends ScmException {
    public NibActivationSourceUserNotFoundException() {
        super("nib_activation_source_user_not_found", "NIB activation source user was not found");
    }
}
