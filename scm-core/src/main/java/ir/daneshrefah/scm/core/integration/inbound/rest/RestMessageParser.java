package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.uaa.common.model.authentication.Authentication;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
@Component
public class RestMessageParser {
    private final AuthenticationClientTemplate authenticationClientTemplate;
    Map<String, BodyExtractor> bodyExtractorMap = new HashMap<>();

    public RestMessageParser(AuthenticationClientTemplate authenticationClientTemplate) {
        bodyExtractorMap.put(HTTP_HEADER_CONTENT_TYPE_JSON, RestMessageParser::bodyExtractorJson);
        this.authenticationClientTemplate = authenticationClientTemplate;
    }

    public Message extractBody(Exchange exchange, TerminalServiceChannelAccess channelAccess) {

        Header header = new Header();
        header.setService(channelAccess);
        header.setContentType(CamelUtils.getContentTypeHeaderFromExchange(exchange));

        String authorizationHeader = CamelUtils.getAuthorizationHeaderFromExchange(exchange);
        String terminalHeader = CamelUtils.getTerminalHeaderFromExchange(exchange);
        Authentication authentication = authenticationClientTemplate
                            .extractAuthenticationFromAuthorizationHeader(terminalHeader, authorizationHeader);
        header.setAuthentication(authentication);

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
        if (null == bodyExtractor) {
            bodyExtractor = RestMessageParser::bodyExtractorNull;
        }
        message = bodyExtractor.transform(exchange, message);
        List<String> pathVariables = extractPathVariables(channelAccess.getTerminalServiceAccess().getService().getAlias());
        for (Iterator<String> iterator = pathVariables.iterator(); iterator.hasNext(); ) {
            String pathVariable = iterator.next();
            String pathVariableValue = exchange.getMessage().getHeader(pathVariable, String.class);
            if (message.getPayload() instanceof NullNode) {
                message.setPayload(JsonNodeFactory.instance.objectNode());
            }
            ((ObjectNode) message.getPayload()).put(pathVariable, pathVariableValue);
        }

        return message;
    }

    public static List<String> extractPathVariables(String urlPattern) {
        List<String> pathVariables = new ArrayList<>();
        if (StringUtils.isEmpty(urlPattern))
            return pathVariables;

        // Define a regular expression pattern to match path variables in curly braces
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(urlPattern);

        // Find and add path variable names to the list
        while (matcher.find()) {
            pathVariables.add(matcher.group(1));
        }

        return pathVariables;
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

    static Message bodyExtractorNull(Exchange exchange, Message message) {
        JsonNode requestBody = NullNode.getInstance();

        message.setPayload(requestBody);

        return message;
    }

}