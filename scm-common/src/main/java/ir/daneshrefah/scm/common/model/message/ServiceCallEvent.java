package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-01
 */
public class ServiceCallEvent extends Event {

    private String serviceCode;
    private ServiceImplementationType implementationType;
    private Object additionalInfo;

    public ServiceCallEvent(LocalDateTime startTime, LocalDateTime endTime, Object error, Object input, Object output, Boolean isSuccessful) {
        super(EventType.SERVICE_CALL, startTime, endTime, error, input, output, isSuccessful);
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public ServiceImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(ServiceImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public Object getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
