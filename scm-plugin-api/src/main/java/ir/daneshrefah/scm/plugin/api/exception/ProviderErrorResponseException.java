package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
@Getter
public class ProviderErrorResponseException extends AbstractServiceProviderException {

    private final String errorCode;
    private final String errorMessage;

    public ProviderErrorResponseException(ExternalServiceProvider provider, String errorCode, String errorMessage) {
        super("invalid provider response", null, provider);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
