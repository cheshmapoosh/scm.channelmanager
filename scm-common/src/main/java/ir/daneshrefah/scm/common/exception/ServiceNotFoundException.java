package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class ServiceNotFoundException extends BaseServiceException {

    public ServiceNotFoundException(String serviceCode) {
        super("no serviceCode[" + serviceCode + "] found.", null, serviceCode);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_SERVICE_CODE_IS_INVALID;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }

}
