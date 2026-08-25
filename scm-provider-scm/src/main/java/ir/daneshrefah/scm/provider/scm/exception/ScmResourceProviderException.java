package ir.daneshrefah.scm.provider.scm.exception;

import ir.daneshrefah.scm.common.exception.ScmException;

/**
 * Stable SCM boundary errors for internal Resource invocation.
 */
public final class ScmResourceProviderException extends ScmException {

    public static final String RESOURCE_NOT_FOUND = "SCM_RESOURCE_NOT_FOUND";
    public static final String ACTION_NOT_FOUND = "SCM_RESOURCE_ACTION_NOT_FOUND";
    public static final String INVALID_ACTION_INPUT = "SCM_RESOURCE_INVALID_INPUT";
    public static final String INVOCATION_FAILED = "SCM_RESOURCE_INVOCATION_FAILED";

    private final String errorCode;

    private ScmResourceProviderException(String errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }

    public static ScmResourceProviderException resourceNotFound() {
        return new ScmResourceProviderException(
                RESOURCE_NOT_FOUND,
                "The requested SCM Resource is not registered."
        );
    }

    public static ScmResourceProviderException actionNotFound() {
        return new ScmResourceProviderException(
                ACTION_NOT_FOUND,
                "The requested SCM Resource Action is not registered."
        );
    }

    public static ScmResourceProviderException invalidActionInput() {
        return new ScmResourceProviderException(
                INVALID_ACTION_INPUT,
                "The SCM Resource Action input is invalid."
        );
    }

    public static ScmResourceProviderException invocationFailed() {
        return new ScmResourceProviderException(
                INVOCATION_FAILED,
                "The SCM Resource Action invocation failed."
        );
    }

    public String getErrorCode() {
        return errorCode;
    }
}
