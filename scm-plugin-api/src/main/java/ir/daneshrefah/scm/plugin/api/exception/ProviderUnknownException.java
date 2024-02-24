package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.utils.string.StringUtils;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_PROVIDER_UNKNOWN_EXCEPTION;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class ProviderUnknownException extends AbstractExternalServiceException {

    public ProviderUnknownException(String serviceCode, String providerCode, Throwable cause) {
        super(String.format(" provider [%s] is unreachable. " + (null != cause ? cause.getMessage() : StringUtils.EMPTY)),
                serviceCode, providerCode, cause);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_PROVIDER_UNKNOWN_EXCEPTION;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER;
    }

}
