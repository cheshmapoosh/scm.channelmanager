package ir.daneshrefah.scm.provider.scm.exception;

import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Stable SCM boundary errors for internal Resource invocation.
 */
public final class ScmResourceProviderException extends ScmException {

    public static final String RESOURCE_NOT_FOUND = "SCM_RESOURCE_NOT_FOUND";
    public static final String ACTION_NOT_FOUND = "SCM_RESOURCE_ACTION_NOT_FOUND";
    public static final String INVALID_ACTION_INPUT = "SCM_RESOURCE_INVALID_INPUT";
    public static final String INVOCATION_FAILED = "SCM_RESOURCE_INVOCATION_FAILED";

    private final String errorCode;
    private final MessageStatus status;

    private ScmResourceProviderException(String errorCode, String message, MessageStatus status) {
        super(errorCode, message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public static ScmResourceProviderException resourceNotFound() {
        return new ScmResourceProviderException(
                RESOURCE_NOT_FOUND,
                "The requested SCM Resource is not registered.",
                MessageStatus.SC_NOT_FOUND
        );
    }

    public static ScmResourceProviderException actionNotFound() {
        return new ScmResourceProviderException(
                ACTION_NOT_FOUND,
                "The requested SCM Resource Action is not registered.",
                MessageStatus.SC_NOT_FOUND
        );
    }

    public static ScmResourceProviderException invalidActionInput() {
        return new ScmResourceProviderException(
                INVALID_ACTION_INPUT,
                "The SCM Resource Action input is invalid.",
                MessageStatus.SC_ERROR_VALIDATION
        );
    }

    public static ScmResourceProviderException invocationFailed() {
        return new ScmResourceProviderException(
                INVOCATION_FAILED,
                "The SCM Resource Action invocation failed.",
                MessageStatus.SC_ERROR_SYSTEM
        );
    }

    public String getErrorCode() {
        return errorCode;
    }

    public MessageStatus getStatus() {
        return status;
    }
}
