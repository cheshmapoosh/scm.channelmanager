package ir.daneshrefah.scm.uaa.exception.activation.nib;

import ir.daneshrefah.scm.common.exception.ScmException;

public class NibActivationDataAccessException extends ScmException {
    public NibActivationDataAccessException(String operation, Throwable cause) {
        super("nib_activation_data_access", "NIB activation database operation failed: " + operation, cause);
    }

    public NibActivationDataAccessException(String operation) {
        super("nib_activation_data_access", "NIB activation database operation failed: " + operation);
    }
}
