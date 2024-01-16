package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class ProviderUnknownException extends AbstractServiceProviderException {

    public ProviderUnknownException(ExternalServiceProvider provider, Throwable cause) {
        super("provider unknown error: " + provider.getCode(), cause, provider);
    }

}
