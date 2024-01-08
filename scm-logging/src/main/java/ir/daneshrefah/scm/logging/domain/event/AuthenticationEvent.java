package ir.daneshrefah.scm.logging.domain.event;

import ir.daneshrefah.scm.common.model.message.Authentication;
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
public class AuthenticationEvent extends DurableEvent<Object, Authentication> {

    @Override
    public EventType getType() {
        return EventType.AUTHENTICATION;
    }
}
