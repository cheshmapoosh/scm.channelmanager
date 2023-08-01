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

    private EventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMillis;
    private String errorMessage;
    private Boolean isSuccessful;

    public Event(EventType type, LocalDateTime startTime, LocalDateTime endTime, String errorMessage, Boolean isSuccessful) {
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMillis = Duration.between(startTime, endTime).toMillis();
        this.errorMessage = errorMessage;
        this.isSuccessful = isSuccessful;
    }

    public EventType getType() {
        return type;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }
}
