package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.plugin.api.inbound.MessageBuilder;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class HttpMessageBuilder extends MessageBuilder<HttpServletRequest> {

    public HttpMessageBuilder(EventProducer eventProducer) {
        super(eventProducer);
    }

    @Override
    protected Event logIncomingRequest(HttpServletRequest input, Object body, TerminalServiceChannelAccess service) {
        return null;
    }

    @Override
    protected Event logOutgoingResponse(Message input) {
        return null;
    }

    @Override
    protected Object extractBody(HttpServletRequest input) {
        return null;
    }

    @Override
    protected Message buildInternal(HttpServletRequest input, Object body, TerminalServiceChannelAccess service) {
        return null;
    }

}
