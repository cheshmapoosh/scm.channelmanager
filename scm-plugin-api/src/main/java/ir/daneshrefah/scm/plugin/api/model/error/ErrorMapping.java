package ir.daneshrefah.scm.plugin.api.model.error;

import ir.daneshrefah.scm.common.model.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.plugin.api.model.message.Status;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
public class ErrorMapping extends BaseModel<String> {

    private ServiceComponentProvider serviceComponentProvider;
    private String providerErrorCode;
    private String scmErrorCode;
    private Status status;
    private String message;

    public ServiceComponentProvider getServiceComponentProvider() {
        return serviceComponentProvider;
    }

    public void setServiceComponentProvider(ServiceComponentProvider serviceComponentProvider) {
        this.serviceComponentProvider = serviceComponentProvider;
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
