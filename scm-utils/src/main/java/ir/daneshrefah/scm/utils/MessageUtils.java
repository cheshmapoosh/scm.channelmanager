package ir.daneshrefah.scm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class MessageUtils {

    public static Message cloneMessage(Message source) {
        Header header = Header.builder()
                .input(source.getHeader().getInput())
//                .authentication(source.getHeader().getAuthentication())
//                .isTransactionAuthenticated(source.getHeader().getIsTransactionAuthenticated())
                .correlationId(source.getHeader().getCorrelationId())
//                .channel(source.getHeader().getChannel())
                .serviceAccess(source.getHeader().getServiceAccess())
                .build();
        Message result = Message.builder()
                .header(header)
                .status(MessageStatus.SC_PROCESSING)
                .payload(source.getPayload())
                .errors(source.getErrors())
                .build();
        return result;
    }

    public static Message generateNestedInternalMessage(Message source, TerminalServiceAccess serviceAccess, JsonNode payload) {
        return generateInternalMessage(source, serviceAccess, payload, source.getHeader().getLevel() + 1, source.getHeader().getMessageId());
    }

    public static Message generateInternalMessage(Message source, TerminalServiceAccess serviceAccess, JsonNode payload) {
        return generateInternalMessage(source, serviceAccess, payload, source.getHeader().getLevel(), null);
    }

    private static Message generateInternalMessage(Message source, TerminalServiceAccess serviceAccess, JsonNode payload,
                                                   int level, String parentMessageId) {
        Header header = Header.builder()
                .input(source.getHeader().getInput())
//                .authentication(source.getHeader().getAuthentication())
//                .isTransactionAuthenticated(source.getHeader().getIsTransactionAuthenticated())
                .correlationId(source.getHeader().getCorrelationId())
//                .channel(source.getHeader().getChannel())
                .serviceAccess(serviceAccess)
                .level(level)
                .parentMessageId(parentMessageId)
                .build();
        Message result = Message.builder()
                .header(header)
                .status(MessageStatus.SC_PROCESSING)
                .payload(null != payload ? payload : NullNode.getInstance())
                .build();
        return result;
    }

}
