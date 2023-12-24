package ir.daneshrefah.scm.core.utils;

import ir.daneshrefah.scm.utils.string.HttpConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-20
 */
public class CamelUtils {

    public static <T> T getHeaderFromMessage(Message message, String name, Class<T> type) {
        return message.getHeader(name, type);
    }

    public static <T> T getHeaderFromExchange(Exchange exchange, String name, Class<T> type) {
        return getHeaderFromMessage(exchange.getMessage(), name, type);
    }

    public static String getStringHeaderFromMessage(Message message, String name) {
        return getHeaderFromMessage(message, name, String.class);
    }

    public static String getStringHeaderFromExchange(Exchange exchange, String name) {
        return getStringHeaderFromMessage(exchange.getMessage(), name);
    }

    public static String getAuthorizationHeaderFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, HttpConstants.HTTP_HEADER_AUTHORIZATION);
    }

    public static String getContentTypeHeaderFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, HttpConstants.HTTP_HEADER_CONTENT_TYPE);
    }

    public static String getTerminalHeaderFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, HttpConstants.HTTP_HEADER_TERMINAL);
    }

}
