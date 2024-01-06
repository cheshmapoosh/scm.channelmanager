package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorReason;
import ir.daneshrefah.scm.common.model.error.ErrorType;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.EventPhase;
import ir.daneshrefah.scm.logging.domain.event.EventType;
import ir.daneshrefah.scm.plugin.api.inbound.MessageBuilder;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.Exchange;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class CamelHttpMessageBuilder extends MessageBuilder<Exchange> {

    private final ObjectMapper objectMapper;

    public CamelHttpMessageBuilder(ObjectMapper objectMapper, EventProducer eventProducer) {
        super(eventProducer);
        this.objectMapper = objectMapper;
    }

    @Override
    protected Event logIncomingRequest(Exchange input, Object body, TerminalServiceChannelAccess service) {
        return Event.builder()
                .correlationId(CamelUtils.getCorrelationFromExchange(input))
                .clientCorrelationId(CamelUtils.getClientCorrelationFromExchange(input))
                .timestamp(Instant.now())
                .username(null)
                .type(EventType.MESSAGE_TRANSFORM)
                .phase(EventPhase.IN)
                .terminalCode(CamelUtils.getTerminalCodeFromExchange(input))
                .clientId(null)
                .threadName(Thread.currentThread().getName())
                .assetIdentifier(null)
                .sourceIdentifier(service.getChannel().getCode())
                .sourceClassName(this.getClass().getSimpleName())
                .accessParameter(CamelUtils.getAccessParameterFromExchange(input))
                .data(body)
                .serverHost(CamelUtils.getServerHostFromExchange(input))
                .targetUrl(CamelUtils.getHttpMethodFromExchange(input) + ":" +
                        CamelUtils.getHttpUrlFromExchange(input))
                .clientAgent(CamelUtils.getClientAgentFromExchange(input))
                .clientUrl(CamelUtils.getRemoteAddressFromExchange(input))
                .build();
    }

    @Override
    protected Event logOutgoingResponse(Message input) {
        return Event.builder()
                .correlationId(input.getHeader().getCorrelationId())
                .clientCorrelationId(input.getHeader().getClientCorrelationId())
                .timestamp(Instant.now())
                .username(input.getHeader().getUsername())
                .type(EventType.MESSAGE_TRANSFORM)
                .phase(EventPhase.OUT)
                .terminalCode(input.getHeader().getService().getTerminalServiceAccess().getTerminal().getCode())
                .clientId(null)
                .threadName(Thread.currentThread().getName())
                .assetIdentifier(null)
                .sourceIdentifier(input.getHeader().getService().getChannel().getCode())
                .sourceClassName(this.getClass().getSimpleName())
                .accessParameter(input.getHeader().getAccessParameter())
                .data(input.getPayload())
                .serverHost(null)
                .targetUrl(null)
                .clientAgent(input.getHeader().getClientAgent())
                .clientUrl(input.getHeader().getClientAddress())
                .build();
    }

    @Override
    protected Object extractBody(Exchange input) {
        return input.getMessage().getBody(String.class);
    }

    @Override
    protected Message buildInternal(Exchange input, Object body, TerminalServiceChannelAccess service) {
        Header header = Header.builder()
                .contentType(CamelUtils.getContentTypeHeaderFromExchange(input))
                .authentication(null)
                .secondLevelAuthenticated(null)
                .correlationId(StringUtils.generateGuid())
                .clientCorrelationId(CamelUtils.getClientCorrelationFromExchange(input))
                .clientTimestamp(CamelUtils.getClientTimestampFromExchange(input))
                .receiveTimestamp(Instant.now())
                .accessParameter(CamelUtils.getAccessParameterFromExchange(input))
                .clientAgent(CamelUtils.getClientAgentFromExchange(input))
                .service(service)
                .clientAddress(CamelUtils.getRemoteAddressFromExchange(input))
                .build();

        if (StringUtils.isEmpty(header.getAccessParameter())) {
            return createValidationErrorMessage(header, Constants.SCM_PARAMETER_ACCESS_PARAMETER, ErrorReason.IS_EMPTY);
        }

        String terminalCode = CamelUtils.getTerminalCodeFromExchange(input);
        if (StringUtils.isNotEmpty(terminalCode) &&
                !StringUtils.equals(service.getTerminalServiceAccess().getTerminal().getCode(), terminalCode)) {
            return createValidationErrorMessage(header, Constants.SCM_PARAMETER_TERMINAL, ErrorReason.IS_INVALID);
        }

        Message message = new Message();
        message.setHeader(header);
        message.setStatus(Status.SC_PROCESSING);
        message.setPayload(extractMessagePayload(input, (String) body, service));

        return message;
    }

    private Message createValidationErrorMessage(Header header, String source, ErrorReason reason) {
        Message result = new Message();
        result.setHeader(header);
        result.addError(new Error(ErrorType.VALIDATION, source, reason), Status.SC_ERROR_VALIDATION);
        result.setPayload(objectMapper.nullNode());
        return result;
    }

    private JsonNode extractMessagePayload(Exchange exchange, String body, TerminalServiceChannelAccess service) {
        JsonNode payload = null;
        if (StringUtils.isNotEmpty(body)) {
            try {
                payload = objectMapper.readTree(body);
            } catch (JsonProcessingException e) {
                LOGGER.error("error extract message body.", e);
            }
        }
        if (null == payload) {
            payload = JsonNodeFactory.instance.nullNode();
        }
        List<String> pathVariables = extractPathVariables(service.getTerminalServiceAccess().getService().getAlias());
        for (Iterator<String> iterator = pathVariables.iterator(); iterator.hasNext(); ) {
            String pathVariable = iterator.next();
            String pathVariableValue = exchange.getMessage().getHeader(pathVariable, String.class);
            if (payload instanceof NullNode) {
                payload = JsonNodeFactory.instance.objectNode();
            }
            ((ObjectNode) payload).put(pathVariable, pathVariableValue);
        }
        return payload;
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


}
