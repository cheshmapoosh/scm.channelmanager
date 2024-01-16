package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class ProviderUnreachableException extends AbstractServiceProviderException {

    public ProviderUnreachableException(ExternalServiceProvider provider, Throwable cause) {
        super("provider unreachable" + provider.getCode(), cause, provider);
    }

}
