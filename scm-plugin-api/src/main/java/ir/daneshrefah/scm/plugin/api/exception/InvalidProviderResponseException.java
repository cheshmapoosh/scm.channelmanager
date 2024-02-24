package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.utils.string.StringUtils;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INVALID_REMOTE_RESPONSE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class InvalidProviderResponseException extends AbstractExternalServiceException {

    public InvalidProviderResponseException(String serviceCode, String providerCode, Throwable cause) {
        super(String.format("invalid provider [%s] response. " + (null != cause ? cause.getMessage() : StringUtils.EMPTY)),
                serviceCode, providerCode, cause);

    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_INVALID_REMOTE_RESPONSE;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER;
    }

}
