package ir.daneshrefah.scm.plugin.api.model.error;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
public class ErrorMapping extends BaseModel<String> {

    private ExternalServiceProvider externalServiceProvider;
    private String exceptionClassName;
    private String providerErrorCode;
    private String scmErrorCode;
    private Status status;
    private String message;

    public ExternalServiceProvider getExternalServiceProvider() {
        return externalServiceProvider;
    }

    public void setExternalServiceProvider(ExternalServiceProvider externalServiceProvider) {
        this.externalServiceProvider = externalServiceProvider;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public void setExceptionClassName(String exceptionClassName) {
        this.exceptionClassName = exceptionClassName;
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    public void setProviderErrorCode(String providerErrorCode) {
        this.providerErrorCode = providerErrorCode;
    }

    public String getScmErrorCode() {
        return scmErrorCode;
    }

    public void setScmErrorCode(String scmErrorCode) {
        this.scmErrorCode = scmErrorCode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
