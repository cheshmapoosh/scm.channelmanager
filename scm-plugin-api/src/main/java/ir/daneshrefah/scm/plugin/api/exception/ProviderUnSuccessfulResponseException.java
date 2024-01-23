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
public class ProviderUnSuccessfulResponseException extends AbstractServiceProviderException {

    private final int statusCode;
    private final String body;

    public ProviderUnSuccessfulResponseException(ExternalServiceProvider provider, int statusCode, String body) {
        super("invalid provider response", null, provider);
        this.statusCode = statusCode;
        this.body = body;
    }

}
