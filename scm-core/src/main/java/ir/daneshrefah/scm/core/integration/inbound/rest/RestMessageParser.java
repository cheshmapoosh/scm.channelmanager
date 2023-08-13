package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.constants.HttpConstants;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.utils.string.StringUtils;
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
public class RestMessageParser {
    Map<String, BodyExtractor> bodyExtractorMap = new HashMap<>();

    public RestMessageParser() {
        bodyExtractorMap.put(HTTP_HEADER_CONTENT_TYPE_JSON, RestMessageParser::bodyExtractorJson);
    }

    public Message extractBody(Exchange exchange, TerminalServiceChannelAccess channelAccess) {

        Header header = new Header();
        header.setService(channelAccess);
        header.setContentType(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, String.class));
        header.setAuthorization(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_AUTHORIZATION, String.class));
        header.setClaimCode(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLAIM, String.class));
        header.setClientCorrelationId(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLIENT_CORRELATION_ID, String.class));
        header.setCorrelationId(StringUtils.generateGuid());
        header.setChannel(channelAccess.getChannel()); //HttpConstants.HTTP_HEADER_CHANNEL
        header.setTerminal(channelAccess.getTerminalServiceAccess().getTerminal()); //HttpConstants.HTTP_HEADER_TERMINAL
//        header.setClientTransactionTimestamp(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CLIENT_TIMESTAMP, String.class));
        header.setAccessParameter(exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_ACCESS_PARAMETER, String.class));
        header.setReceiveTimestamp(LocalDateTime.now());

        Message message = new Message();
        message.setHeader(header);
        message.setStatus(Status.SC_PROCESSING);

        String contentType = exchange.getMessage().getHeader(HttpConstants.HTTP_HEADER_CONTENT_TYPE, String.class);
        BodyExtractor bodyExtractor = bodyExtractorMap.get(contentType);
        if (null != bodyExtractor) {
            message = bodyExtractor.transform(exchange, message);
        }

        return message;
    }

    @FunctionalInterface
    interface BodyExtractor {
        Message transform(Exchange exchange, Message message);
    }

    // Define your transformation methods
    static Message bodyExtractorJson(Exchange exchange, Message message) {
        JsonNode requestBody = exchange.getMessage().getBody(JsonNode.class);

        message.setPayload(requestBody);

        return message;
    }

}