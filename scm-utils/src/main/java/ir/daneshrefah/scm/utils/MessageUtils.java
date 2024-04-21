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

    public static String getTerminalCode(Message message) {
        if (null == message || null == message.getHeader()) {
            return null;
        }
        return message.getHeader().getRequest().getTerminalCode();
    }

    public static String getClientId(Message message) {
        if (null == message || null == message.getHeader()) {
            return null;
        }
        return message.getHeader().getRequest().getClientId();
    }

//    public static List<Difference> calcDifference(Message from, Message to) {
//        List<Difference> differences = new ArrayList<>();
//        for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(from.getClass()).getPropertyDescriptors()) {
//            Object fromValue = propertyDescriptor.getReadMethod().invoke(from);
//            Object toValue = propertyDescriptor.getReadMethod().invoke(to);
//            if (!Objects.equals(fromValue, toValue)) {
//                differences.add(new Difference(propertyDescriptor.getName(), fromValue, toValue));
//            }
//            if (fromValue instanceof Comparable && fromValue.getClass().equals(toValue.getClass())) {
//                // Handle nested comparisons for Comparable objects of the same class
//                differences.addAll(compareProperties(fromValue, toValue));
//            }
//        }
//        return differences;
//    }

    public static Message cloneMessage(Message source) {
        Header header = Header.builder()
                .request(source.getHeader().getRequest())
                .authentication(source.getHeader().getAuthentication())
                .isTransactionAuthenticated(source.getHeader().isTransactionAuthenticated())
                .correlationId(source.getHeader().getCorrelationId())
                .channel(source.getHeader().getChannel())
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
                .request(source.getHeader().getRequest())
                .authentication(source.getHeader().getAuthentication())
                .isTransactionAuthenticated(source.getHeader().isTransactionAuthenticated())
                .correlationId(source.getHeader().getCorrelationId())
                .channel(source.getHeader().getChannel())
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

    public static boolean isContinueAllowed(Message message) {
        return null != message && MessageStatus.SC_PROCESSING.equals(message.getStatus());
    }

    public static boolean isSuccessful(Message message) {
        return null != message && MessageStatus.SC_SUCCESS.equals(message.getStatus());
    }

    public static Message getCurrentMessage() {
        return MessageContext.getCurrentContext().getMessage();
    }

    public static String getCurrentTerminalCode() {
        return getTerminalCode(getCurrentMessage());
    }

    public static boolean isTransactionAuthenticated() {
        return isTransactionAuthenticated(getCurrentMessage());
    }

    public static boolean isTransactionAuthenticated(Message message) {
        return null != message && message.getHeader().isTransactionAuthenticated();
    }

    public boolean hasAuthority(String authority) {
        return hasAuthority(getCurrentMessage(), authority);
    }

    public static Authentication getAuthentication(Message message) {
        return null != message && null != message.getHeader() && null != message.getHeader().getAuthentication() &&
                message.getHeader().getAuthentication().isFullyAuthenticated() ? message.getHeader().getAuthentication() : null;
    }
    public boolean hasAuthority(Message message, String authority) {
        return null != message && message.getHeader().getAuthentication().isAuthenticated() &&
                message.getHeader().getAuthentication().hasAuthority(authority);
    }

    public static String getUsername(Message message) {
        Authentication authentication = getAuthentication(message);
        if (Objects.isNull(authentication)) {
            return null;
        }
        return message.getHeader().getAuthentication().getProfile().getNickname();
    }

    public static String getCSPUsername(Message message) {
        Authentication authentication = getAuthentication(message);
        if (Objects.isNull(authentication) || !authentication.isDelegated()) {
            return null;
        }
        return authentication.getName();
    }

    public static boolean isDelegated(Message message) {
        Authentication authentication = getAuthentication(message);
        return Objects.nonNull(authentication) && authentication.isDelegated();
    }

}
