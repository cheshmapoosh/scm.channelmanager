package ir.daneshrefah.scm.logging.api;

import ir.daneshrefah.scm.common.model.event.Event;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-04
 */
public abstract class EventProducer {

    private static EventProducer singletonInstance;

    public EventProducer() {
        if (null != singletonInstance) {
            throw new RuntimeException("eventProducer instance already exist with type: " +
                    singletonInstance.getClass().getName());
        }
        singletonInstance = this;
    }

    public abstract void sendEvent(Event event);

    public static final EventProducer getInstance() {
        return singletonInstance;
    }

}
