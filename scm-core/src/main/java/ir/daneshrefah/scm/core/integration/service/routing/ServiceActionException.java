package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Stable SCM boundary failures for ACTION_DISPATCH selection and configuration.
 */
public final class ServiceActionException extends ScmException {
    public static final String ACTION_REQUIRED = "SCM_SERVICE_ACTION_REQUIRED";
    public static final String ACTION_NOT_FOUND = "SCM_SERVICE_ACTION_NOT_FOUND";
    public static final String CONFIGURATION_INVALID = "SCM_SERVICE_ACTION_CONFIGURATION_INVALID";

    private final String errorCode;
    private final String publicMessage;
    private final MessageStatus status;

    private ServiceActionException(
            String errorCode,
            String diagnosticMessage,
            String publicMessage,
            MessageStatus status,
            Throwable cause
    ) {
        super(errorCode, diagnosticMessage, cause);
        this.errorCode = errorCode;
        this.publicMessage = publicMessage;
        this.status = status;
    }

    public static ServiceActionException actionRequired() {
        return new ServiceActionException(
                ACTION_REQUIRED,
                "A service action was not selected for ACTION_DISPATCH.",
                "A service action is required.",
                MessageStatus.SC_ERROR_VALIDATION,
                null
        );
    }

    public static ServiceActionException actionNotFound() {
        return new ServiceActionException(
                ACTION_NOT_FOUND,
                "The selected service action is not registered for ACTION_DISPATCH.",
                "The requested service action was not found.",
                MessageStatus.SC_NOT_FOUND,
                null
        );
    }

    public static ServiceActionException configurationInvalid(String diagnosticMessage) {
        return configurationInvalid(diagnosticMessage, null);
    }

    public static ServiceActionException configurationInvalid(
            String diagnosticMessage,
            Throwable cause
    ) {
        return new ServiceActionException(
                CONFIGURATION_INVALID,
                diagnosticMessage,
                "The service action configuration is invalid.",
                MessageStatus.SC_ERROR_SYSTEM,
                cause
        );
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getPublicMessage() {
        return publicMessage;
    }

    public MessageStatus getStatus() {
        return status;
    }
}
