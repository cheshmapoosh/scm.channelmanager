package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-01
 */
public class AuthenticationEvent extends Event {

    private String username;

    public AuthenticationEvent(LocalDateTime startTime, LocalDateTime endTime, Object error, Object input, Object output, Boolean isSuccessful) {
        super(EventType.AUTHENTICATION, startTime, endTime, error, input, output, isSuccessful);
    }

}
