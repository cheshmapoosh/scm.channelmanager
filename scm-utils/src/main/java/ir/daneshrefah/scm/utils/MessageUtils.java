package ir.daneshrefah.scm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class MessageUtils {

    public static String getTerminalCode(Message message) {
        if (null == message || null == message.getHeader()) {
            return null;
        }
        return message.getHeader().getRequest().getTerminalCode();
    }

    public static Message generateInternalMessage(Message source, TerminalServiceAccess serviceAccess, JsonNode payload) {
        Header header = Header.builder()
                .request(source.getHeader().getRequest())
                .authentication(source.getHeader().getAuthentication())
                .isTransactionAuthenticated(source.getHeader().isTransactionAuthenticated())
                .correlationId(source.getHeader().getCorrelationId())
                .channel(source.getHeader().getChannel())
                .serviceAccess(serviceAccess)
                .build();
        Message result = Message.builder()
                .header(header)
                .status(Status.SC_PROCESSING)
                .payload(null != payload ? payload : NullNode.getInstance())
                .build();
        return result;
    }

    public static boolean isContinueAllowed(Message message) {
        return null != message && Status.SC_PROCESSING.equals(message.getStatus());
    }

}
