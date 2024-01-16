package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class InvalidProviderResponseException extends AbstractServiceProviderException {

    public InvalidProviderResponseException(Throwable cause, ExternalServiceProvider provider) {
        super("invalid provider response", cause, provider);
    }

}
