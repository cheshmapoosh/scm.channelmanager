package ir.daneshrefah.scm.logging.domain.event;

import ir.daneshrefah.scm.common.model.message.Authentication;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
@SuperBuilder
public class VoteEvent extends DurableEvent<String, Integer> {

    @Override
    public EventType getType() {
        return EventType.VOTE;
    }
}
