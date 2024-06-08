package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseServiceException;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-15
 */
public abstract class AbstractExternalServiceException extends BaseServiceException {

    @Getter
    private final String providerCode;

    public AbstractExternalServiceException(String message, String serviceCode, String providerCode, Throwable cause) {
        super(message, cause, serviceCode);
        this.providerCode = providerCode;
    }

    @Override
    public String getSource() {
        return providerCode + "[" + getServiceCode() + "]";
    }

}
