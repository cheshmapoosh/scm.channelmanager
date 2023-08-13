package ir.daneshrefah.scm.common.model.message;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class Event implements Serializable {

    private EventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMillis;
    private Object error;
    private Object input;
    private Object output;
    private Boolean isSuccessful;

    public Event(EventType type, LocalDateTime startTime, LocalDateTime endTime, Object error, Object input,
                 Object output, Boolean isSuccessful) {
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMillis = Duration.between(startTime, endTime).toMillis();
        this.error = error;
        this.input = input;
        this.output = output;
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

    public Object getError() {
        return error;
    }

    public Boolean getSuccessful() {
        return isSuccessful;
    }

    public Object getInput() {
        return input;
    }

    public Object getOutput() {
        return output;
    }
}
