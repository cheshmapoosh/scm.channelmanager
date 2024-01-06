package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public abstract class MessageBuilder<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MessageBuilder.class);

    private final EventProducer eventProducer;

    protected MessageBuilder(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    protected abstract Event logIncomingRequest(T input, Object body, TerminalServiceChannelAccess service);

    protected abstract Event logOutgoingResponse(Message input);

    public final Message build(T input, TerminalServiceChannelAccess service){
        Object body = extractBody(input);
        Event incomingEvent = logIncomingRequest(input, body, service);
        eventProducer.sendEvent(incomingEvent);

        Message result = buildInternal(input, body, service);

        Event outgoingEvent = logOutgoingResponse(result);
        eventProducer.sendEvent(outgoingEvent);

        return result;
    }

    protected abstract Object extractBody(T input);

    protected abstract Message buildInternal(T input, Object body, TerminalServiceChannelAccess service);

}
