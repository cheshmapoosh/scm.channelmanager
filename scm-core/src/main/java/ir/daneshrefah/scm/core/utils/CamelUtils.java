package ir.daneshrefah.scm.core.utils;

import ir.daneshrefah.scm.common.exception.InvalidInputDateFormatException;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.time.Instant;

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

    public static String getUsernameHeaderFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_USERNAME);
    }

    public static String getClaimCodeFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_CLAIM_CODE);
    }

    public static String getClientCorrelationFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID);
    }

    public static String getCorrelationFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_CORRELATION_ID);
    }

    public static String getAccessParameterFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_ACCESS_PARAMETER);
    }

    public static String getAuthorizationHeaderFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_AUTHORIZATION);
    }

    public static String getContentTypeHeaderFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, HttpConstants.HTTP_HEADER_CONTENT_TYPE);
    }

    public static String getTerminalCodeFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_TERMINAL);
    }

    public static String getClientIdFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_CLIENT_ID);
    }

    public static String getClientAgentFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, HttpConstants.HTTP_HEADER_USER_AGENT);
    }

    public static Instant getClientTimestampFromExchange(Exchange exchange) {
// sample value: 2024-01-06T05:15:50.854476Z
        String clientTimestamp = getStringHeaderFromExchange(exchange, Constants.SCM_PARAMETER_CLIENT_TIMESTAMP);
        if (StringUtils.isEmpty(clientTimestamp))
            return null;
        try {
            return Instant.parse(clientTimestamp);
        } catch (Exception e) {
            throw new InvalidInputDateFormatException(Constants.SCM_PARAMETER_CLIENT_TIMESTAMP);
        }
    }

    public static String getServerHostFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, HttpConstants.HTTP_HEADER_HOST);
    }

    public static String getHttpUrlFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.CAMEL_PARAMETER_HTTP_URL);
    }

    public static String getHttpMethodFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.CAMEL_PARAMETER_HTTP_METHOD);
    }

    public static String getRemoteAddressFromExchange(Exchange exchange) {
        return getStringHeaderFromExchange(exchange, Constants.CAMEL_PARAMETER_HTTP_REMOTE_ADDRESS);
    }

    public static Status getStatus(Exchange exchange) {
        return exchange.getMessage().getBody(ir.daneshrefah.scm.common.model.message.Message.class).getStatus();
    }

    public static boolean isInProgress(Exchange exchange) {
        return Status.SC_PROCESSING.equals(exchange.getMessage().getBody(
                ir.daneshrefah.scm.common.model.message.Message.class).getStatus());
    }

}
