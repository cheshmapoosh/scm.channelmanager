package ir.daneshrefah.scm.logging.domain.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-07
 */
@Getter
@SuperBuilder
public class TransformEvent extends DurableEvent<Object, Object> {

    @Override
    public EventType getType() {
        return EventType.TRANSFORM;
    }

}
