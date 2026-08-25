package ir.daneshrefah.scm.provider.rest.config;

final class RestProviderTargetValidationException extends IllegalArgumentException {

    enum OperationPathType {
        ABSOLUTE,
        RELATIVE,
        MISSING,
        INVALID
    }

    enum Reason {
        MISSING_OPERATION_PATH,
        BASE_URL_AND_ABSOLUTE_PATH_CONFLICT,
        RELATIVE_PATH_WITHOUT_BASE_URL,
        INVALID_BASE_URL,
        INVALID_ABSOLUTE_OPERATION_URL,
        INVALID_RELATIVE_OPERATION_PATH,
        INVALID_EFFECTIVE_TARGET
    }

    private final OperationPathType operationPathType;
    private final Reason reason;

    RestProviderTargetValidationException(
            OperationPathType operationPathType,
            Reason reason,
            String message
    ) {
        super(message);
        this.operationPathType = operationPathType;
        this.reason = reason;
    }

    OperationPathType operationPathType() {
        return operationPathType;
    }

    Reason reason() {
        return reason;
    }
}
