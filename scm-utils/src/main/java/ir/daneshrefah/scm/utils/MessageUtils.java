package ir.daneshrefah.scm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

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
        return message.getHeader().getTerminalCode();
    }

    public static Message generateInternalMessage(Message source, TerminalServiceAccess serviceAccess, JsonNode payload) {
        Header header = Header.builder()
                .contentType(source.getHeader().getContentType())
                .authentication(source.getHeader().getAuthentication())
                .isTransactionAuthenticated(source.getHeader().isTransactionAuthenticated())
                .correlationId(source.getHeader().getCorrelationId())
                .clientCorrelationId(source.getHeader().getClientCorrelationId())
                .clientTimestamp(source.getHeader().getClientTimestamp())
                .receiveTimestamp(source.getHeader().getReceiveTimestamp())
                .accessParameter(source.getHeader().getAccessParameter())
                .clientAgent(source.getHeader().getClientAgent())
                .serverHost(source.getHeader().getServerHost())
                .serviceAccess(serviceAccess)
                .channel(source.getHeader().getChannel())
                .clientAddress(source.getHeader().getClientAddress())
                .build();
        Message result = new Message(source.getRequest());
        result.setHeader(header);
        result.setStatus(Status.SC_PROCESSING);
        result.setPayload(null != payload ? payload : NullNode.getInstance());
        return result;
    }

}
