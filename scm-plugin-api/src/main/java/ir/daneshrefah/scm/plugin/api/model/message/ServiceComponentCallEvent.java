package ir.daneshrefah.scm.plugin.api.model.message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-01
 */
public class ServiceComponentCallEvent extends Event {

    private String serviceComponentCode;
    private String serviceProviderCode;
    private String errorMessage;
    private Boolean isSuccessful;

    public ServiceComponentCallEvent(String serviceCode, String serviceProviderCode, String errorMessage, Boolean isSuccessful) {
        this.serviceComponentCode = serviceCode;
        this.serviceProviderCode = serviceProviderCode;
        this.errorMessage = errorMessage;
        this.isSuccessful = isSuccessful;
    }

    public String getServiceComponentCode() {
        return serviceComponentCode;
    }

    public String getServiceProviderCode() {
        return serviceProviderCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }
}
