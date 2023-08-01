package ir.daneshrefah.scm.plugin.api.model.message;

import java.time.LocalDateTime;

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

    public ServiceComponentCallEvent(LocalDateTime startTime, LocalDateTime endTime, String errorMessage, Boolean isSuccessful) {
        super(EventType.SERVICE_COMPONENT_CALL, startTime, endTime, errorMessage, isSuccessful);
    }

    public String getServiceComponentCode() {
        return serviceComponentCode;
    }

    public void setServiceComponentCode(String serviceComponentCode) {
        this.serviceComponentCode = serviceComponentCode;
    }

    public String getServiceProviderCode() {
        return serviceProviderCode;
    }

    public void setServiceProviderCode(String serviceProviderCode) {
        this.serviceProviderCode = serviceProviderCode;
    }
}
