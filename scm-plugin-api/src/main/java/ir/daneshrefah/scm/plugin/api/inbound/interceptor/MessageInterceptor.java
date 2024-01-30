package ir.daneshrefah.scm.plugin.api.inbound.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public abstract class MessageInterceptor {

    public final Message intercept(Message message) {
        Instant startTime = Instant.now();
        message = internalIntercept(message);
        return message;
    }

    protected abstract Message internalIntercept(Message message);

}
