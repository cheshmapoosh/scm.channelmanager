package ir.daneshrefah.scm.core.integration.inbound.rest;

import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import org.apache.camel.Exchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
public class RestResponseGenerator {
    Map<Status, Integer> statusMappingMap = new HashMap<>();

    public RestResponseGenerator() {
        statusMappingMap.put(Status.SC_PROCESSING, 200);
        statusMappingMap.put(Status.SC_SUCCESS, 200);
        statusMappingMap.put(Status.SC_UNAUTHORIZED, 401);
        statusMappingMap.put(Status.SC_NOT_FOUND, 404);
        statusMappingMap.put(Status.SC_ERROR_VALIDATION, 400);
        statusMappingMap.put(Status.SC_ERROR_SYSTEM, 500);
        statusMappingMap.put(Status.SC_ERROR_BUSINESS, 400);
        statusMappingMap.put(Status.SC_ERROR_UNAVAILABLE_PROVIDER, 502);
    }

    public void initResponseHeader(Exchange exchange) {
        Message message = exchange.getMessage().getBody(Message.class);
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, statusMappingMap.get(message.getStatus()));
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, message.getHeader().getContentType());
        exchange.getMessage().setHeader(HttpConstants.HTTP_HEADER_CLIENT_CORRELATION_ID, message.getHeader().getClientCorrelationId());
        exchange.getMessage().setHeader(HttpConstants.HTTP_HEADER_CORRELATION_ID, message.getHeader().getCorrelationId());
        exchange.getMessage().setHeader(HttpConstants.HTTP_HEADER_CLIENT_TIMESTAMP, message.getHeader().getClientTransactionTimestamp());
        exchange.getMessage().setHeader(HttpConstants.HTTP_HEADER_RECEIVE_TIMESTAMP, message.getHeader().getReceiveTimestamp());
    }

}