package ir.daneshrefah.scm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
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
        return message.getHeader().getRequest().getTerminalCode();
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

}
