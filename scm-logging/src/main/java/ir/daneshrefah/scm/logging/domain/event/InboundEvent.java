package ir.daneshrefah.scm.logging.domain.event;

import ir.daneshrefah.scm.common.model.message.Message;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-07
 */
@SuperBuilder
public class InboundEvent extends Event<Message> {

    @Override
    public EventType getType() {
        return EventType.INBOUND;
    }
}
