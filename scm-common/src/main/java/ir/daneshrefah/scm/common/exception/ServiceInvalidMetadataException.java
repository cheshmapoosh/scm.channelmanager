package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.apache.commons.lang3.StringUtils;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INVALID_SERVICE_METADATA;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public class ServiceInvalidMetadataException extends BaseServiceException {

    public ServiceInvalidMetadataException(String serviceCode, Exception cause) {
        this(serviceCode, "service [" + serviceCode + "] has invalid metadata. " +
                        (null != cause ? StringUtils.isNotEmpty(cause.getMessage()) ? cause.getMessage() : cause.getClass().getSimpleName() : StringUtils.EMPTY),
                cause);
    }

    public ServiceInvalidMetadataException(String serviceCode, String message) {
        this(serviceCode, message, null);
    }

    public ServiceInvalidMetadataException(String serviceCode, String message, Exception cause) {
        super(StringUtils.isNotEmpty(message) ? message : "service [" + serviceCode + "] has invalid metadata. " +
                        (null != cause ? StringUtils.isNotEmpty(cause.getMessage()) ? cause.getMessage() : cause.getClass().getSimpleName() : StringUtils.EMPTY),
                cause, serviceCode);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_INVALID_SERVICE_METADATA;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }

}
