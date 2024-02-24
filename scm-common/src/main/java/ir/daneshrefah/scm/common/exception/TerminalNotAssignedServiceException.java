package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class TerminalNotAssignedServiceException extends BaseServiceException {

    public TerminalNotAssignedServiceException(Service service, String terminalCode) {
        this(String.format("service '%s' not assigned to terminal '%s'.", service.getCode(), terminalCode), service);
    }

    public TerminalNotAssignedServiceException(String message, Service service) {
        super(message, null, service);
    }

    @Override
    public int getErrorCode() {
        return ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_ASSIGNED_TO_TERMINAL;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }
}
