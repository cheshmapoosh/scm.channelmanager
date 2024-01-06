package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
public class ExternalProviderException extends BaseException {

    private ExternalServiceProvider externalServiceProvider;
    private String providerErrorCode;
    private String providerErrorMessage;

    public ExternalProviderException(ExternalServiceProvider externalServiceProvider, String providerErrorCode,
                                     String providerErrorMessage) {
        super(null, externalServiceProvider.getClass().getSimpleName());
        this.externalServiceProvider = externalServiceProvider;
        this.providerErrorCode = providerErrorCode;
        this.providerErrorMessage = providerErrorMessage;
    }

    public String getSourceErrorCode() {
        return providerErrorCode;
    }

    public String getSourceErrorMessage() {
        return providerErrorMessage;
    }
}
