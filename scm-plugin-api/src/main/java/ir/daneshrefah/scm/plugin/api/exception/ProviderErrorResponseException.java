package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class ProviderErrorResponseException extends AbstractExternalServiceException {

    private final String errorCode;
    @Getter
    private final String errorMessage;

    public ProviderErrorResponseException(String serviceCode, String providerCode, String errorCode, String errorMessage) {
        this(serviceCode, providerCode, errorCode, errorMessage, null);
    }

    public ProviderErrorResponseException(String serviceCode, String providerCode, String errorCode, String errorMessage, Exception cause) {
        super(String.format("invalid provider [%s] response. (" + (null != errorMessage ? errorMessage : "null") + ")", providerCode) + (null != cause ? cause.getMessage() : StringUtils.EMPTY),
                serviceCode, providerCode, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getRemoteErrorCode() {
        return errorCode;
    }

    @Override
    public int getErrorCode() {
        return 0;
    }

    @Override
    public MessageStatus getStatus() {
        return null;
    }
}
