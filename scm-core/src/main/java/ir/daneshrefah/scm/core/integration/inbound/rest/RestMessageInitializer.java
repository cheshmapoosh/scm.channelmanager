package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.plugin.api.model.message.Header;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import org.apache.camel.Exchange;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.plugin.api.constants.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
public class RestMessageInitializer {
    Map<String, BodyExtractor> bodyExtractorMap = new HashMap<>();

    public RestMessageInitializer() {
        bodyExtractorMap.put(HTTP_HEADER_CONTENT_TYPE_JSON, RestMessageInitializer::bodyExtractorJson);
    }

    public void initMessageBody(Exchange exchange, TerminalServiceChannelAccess channelAccess) {
        String contentType = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, String.class);
        BodyExtractor bodyExtractor = bodyExtractorMap.get(contentType);
        Message message = null;
        if (null != bodyExtractor) {
            message = bodyExtractor.transform(exchange, channelAccess);
        }
        exchange.getMessage().setBody(message, Message.class);
    }

    @FunctionalInterface
    interface BodyExtractor {
        Message transform(Exchange exchange, TerminalServiceChannelAccess channelAccess);
    }

    // Define your transformation methods
    static Message bodyExtractorJson(Exchange exchange, TerminalServiceChannelAccess channelAccess) {
        JsonNode requestBody = exchange.getMessage().getBody(JsonNode.class);
        Message message = new Message();
        Header header = new Header();
        header.setService(channelAccess);
        header.setContentType(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, String.class));
        header.setAuthorization(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION, String.class));
        header.setClaimCode(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLAIM, String.class));
        header.setCorrelationId(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CORRELATION_ID, String.class));
        header.setChannel(channelAccess.getChannel()); //HttpConstants.HTTP_HEADER_CHANNEL
        header.setTerminal(channelAccess.getTerminalServiceAccess().getTerminal()); //HttpConstants.HTTP_HEADER_TERMINAL
//        header.setClientTransactionTimestamp(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLIENT_TIMESTAMP, String.class));
        header.setAccessParameter(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_ACCESS_PARAMETER, String.class));
        header.setReceiveTimestamp(LocalDateTime.now());
        message.setHeader(header);
        message.setStatus(Status.SC_PROCESSING);

        message.setPayload(requestBody);

        return message;
    }

}