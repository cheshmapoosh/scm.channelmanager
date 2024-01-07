package ir.daneshrefah.scm.core.integration.inbound.rest;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.plugin.api.inbound.MessageBuilder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class HttpMessageBuilder extends MessageBuilder<HttpServletRequest> {

    @Override
    protected Object extractBody(HttpServletRequest input) {
        return null;
    }

    @Override
    protected Message buildInternal(HttpServletRequest input, Object body, TerminalServiceChannelAccess service) {
        return null;
    }

}
