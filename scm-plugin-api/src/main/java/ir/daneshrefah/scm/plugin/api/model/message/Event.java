package ir.daneshrefah.scm.plugin.api.model.message;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class Event {

    private String serviceCode;
    private String serviceProviderCode;
    private EventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMillis;

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceProviderCode() {
        return serviceProviderCode;
    }

    public void setServiceProviderCode(String serviceProviderCode) {
        this.serviceProviderCode = serviceProviderCode;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
