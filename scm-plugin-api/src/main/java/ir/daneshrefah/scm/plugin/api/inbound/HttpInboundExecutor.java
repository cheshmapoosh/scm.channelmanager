package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public interface HttpInboundExecutor {

    TerminalServiceChannelAccess findService(HttpServletRequest request, String serviceCode);

    public Message executeService(HttpServletRequest request, String serviceCode, JsonNode payload);

}
