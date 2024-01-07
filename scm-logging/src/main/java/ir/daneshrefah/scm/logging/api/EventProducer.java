package ir.daneshrefah.scm.logging.api;

import ir.daneshrefah.scm.logging.domain.event.Event;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-04
 */
public abstract class EventProducer {

    protected static EventProducer instance;

    public abstract void sendEvent(Event event);

    public static final EventProducer getInstance() {
        return instance;
    }

}
