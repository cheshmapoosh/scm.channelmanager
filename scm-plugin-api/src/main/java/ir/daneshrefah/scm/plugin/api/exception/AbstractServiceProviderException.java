package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public class AbstractServiceProviderException extends BaseException {

    @Getter
    private final ExternalServiceProvider provider;

    public AbstractServiceProviderException(String message, Throwable cause, ExternalServiceProvider provider) {
        super(message, cause);
        this.provider = provider;
    }

}
