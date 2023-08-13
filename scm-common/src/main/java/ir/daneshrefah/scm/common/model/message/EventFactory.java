package ir.daneshrefah.scm.common.model.message;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class EventFactory {

    public static Event createNewEvent(EventType type, LocalDateTime startTime, LocalDateTime endTime, Object error,
                                       Object input, Object output, Boolean isSuccessful) {
        Event event = null;
        switch (type) {
            case WHOLE:
                event = new Event(type, startTime, endTime, error, input, output, isSuccessful);
                break;
            case TRANSFORM:
                event = new TransformEvent(startTime, endTime, error, input, output, isSuccessful);
                break;
            case SERVICE_CALL:
                event = new ServiceCallEvent(startTime, endTime, error, input, output, isSuccessful);
                break;
        }
        return event;
    }
}
