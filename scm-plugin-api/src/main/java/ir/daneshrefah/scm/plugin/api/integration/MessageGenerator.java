package ir.daneshrefah.scm.plugin.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.message.*;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.utils.MessageContext;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-06
 */
@RequiredArgsConstructor
@Component
public class MessageGenerator {

    private final TerminalService terminalService;
    private final ObjectMapper objectMapper;

    public Message buildEmptyMessage(MessageInput input, Channel channel, Exception exception) {
        String inputTerminalCode = input.getHeader(SCM_PARAMETER_TERMINAL);
        TerminalServiceAccess serviceAccess = findServiceAccess(inputTerminalCode, input.getServiceCode());
//        MessageRequestInfo requestInfo = MessageRequestInfo.builder()
//                .input(input)
//                .terminalCode(inputTerminalCode)
//                .clientId(input.getHeader(SCM_PARAMETER_CLIENT_ID))
//                .serviceCode(input.getServiceCode())
//                .contentType(input.getContentType())
////                .clientRemoteAddress(input.getClientRemoteAddress())
//                .clientCorrelationId(input.getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID))
//                .clientTimestamp(null)
////                .clientAgent(input.getClientAgent())
//                .accessParameter(input.getHeader(SCM_PARAMETER_ACCESS_PARAMETER))
//                .username(input.getHeader(SCM_PARAMETER_USERNAME))
//                .authenticationType(null)
//                .authenticationValue(null)
//                .transactionAuthenticationType(null)
//                .transactionAuthenticationValue(input.getHeader(SCM_PARAMETER_CLAIM_CODE))
//                .receiveTimestamp(input.getReceiveTimestamp())
//                .serverHost(input.getServerHost())
//                .payload(null)
//                .isForCheck(input.isForCheck())
//                .error(exception)
//                .build();
        Header header = Header.builder()
                .input(input)
                .channel(channel)
                .serviceAccess(serviceAccess)
                .build();

        Message message = Message.builder()
                .header(header)
                .status(input.isForCheck() ? MessageStatus.SC_SUCCESS : MessageStatus.SC_PROCESSING)
                .payload(null)
                .build();
        MessageContext.init(message);

        return message;
    }

    public Message buildMessageInternal(MessageInput input, Channel channel) throws Exception {
        String inputTerminalCode = input.getTerminalCode();
        String inputAccessParameter = input.getHeader(SCM_PARAMETER_ACCESS_PARAMETER);
        if (!input.isForCheck() && StringUtils.isEmpty(inputTerminalCode)) {
            throw new MissingRequiredInputException(SCM_PARAMETER_TERMINAL);
        }
        if (StringUtils.isEmpty(input.getServiceCode())) {
            throw new ServiceNotFoundException(input.getServiceCode());
        }
        TerminalServiceAccess serviceAccess = findServiceAccess(inputTerminalCode, input.getServiceCode());
        if (!input.isForCheck() && null == serviceAccess) {
            throw new TerminalServiceNotFoundException(inputTerminalCode, input.getServiceCode());
        }
        if (!input.isForCheck() && StringUtils.isEmpty(inputAccessParameter)) {
            throw new MissingRequiredInputException(SCM_PARAMETER_ACCESS_PARAMETER);
        }
        if (!input.isForCheck() && !StringUtils.equalsIgnoreCase(serviceAccess.getTerminal().getCode(), inputTerminalCode)) {
            throw new InvalidInputException(SCM_PARAMETER_TERMINAL);
        }

//        String inputClientTimestamp = input.getHeader(SCM_PARAMETER_CLIENT_TIMESTAMP);
//        String inputClaimCode = input.getHeader(SCM_PARAMETER_CLAIM_CODE);
//        Instant clientTimestamp = StringUtils.isEmpty(inputClientTimestamp) ? null :
//                DateUtils.InstantTools.convertToInstant(inputClientTimestamp); //throw exception
//        MessageRequestInfo requestInfo = MessageRequestInfo.builder()
//                .input(input)
//                .terminalCode(!input.isForCheck() ? serviceAccess.getTerminal().getCode() : inputTerminalCode)
//                .clientId(input.getHeader(SCM_PARAMETER_CLIENT_ID))
//                .serviceCode(input.getServiceCode())
//                .contentType(input.getContentType())
////                .clientRemoteAddress(input.getClientRemoteAddress())
//                .clientCorrelationId(input.getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID))
//                .clientTimestamp(clientTimestamp)
////                .clientAgent(input.getClientAgent())
//                .accessParameter(inputAccessParameter)
//                .username(input.getHeader(SCM_PARAMETER_USERNAME))
//                .authenticationType(extractAuthenticationType(input))
//                .authenticationValue(extractAuthenticationValue(input))
//                .transactionAuthenticationType(StringUtils.isNotEmpty(inputClaimCode) ? ClientAuthenticationType.BASIC : ClientAuthenticationType.ANONYMOUS)
//                .transactionAuthenticationValue(inputClaimCode)
//                .receiveTimestamp(input.getReceiveTimestamp())
//                .serverHost(input.getServerHost())
//                .payload(extractMessagePayload(serviceAccess, input))
//                .isForCheck(input.isForCheck())
//                .build();
        Header header = Header.builder()
                .input(input)
                .authentication(null)
                .isTransactionAuthenticated(false)
                .correlationId(StringUtils.generateGuid())
                .channel(channel)
                .serviceAccess(serviceAccess)
                .build();

        Message message = Message.builder()
                .header(header)
                .status(input.isForCheck() ? MessageStatus.SC_SUCCESS : MessageStatus.SC_PROCESSING)
                .payload(extractMessagePayload(serviceAccess, input))
                .build();
        MessageContext.init(message);

        return message;
    }

    private TerminalServiceAccess findServiceAccess(String terminalCode, String serviceCode) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(serviceCode))
            return null;
        return terminalService.findTerminalServiceAccessByTerminalCodeAndServiceCode(terminalCode, serviceCode).orElse(null);
    }

    private ClientAuthenticationType extractAuthenticationType(MessageInput messageInput) {
        String authorizationHeader = null != messageInput ? messageInput.getHeader(SCM_PARAMETER_AUTHORIZATION) : null;
        if (StringUtils.isEmpty(authorizationHeader)) {
            return ClientAuthenticationType.ANONYMOUS;
        }

        String AUTHENTICATION_SCHEME_BASIC = "Basic";
        String AUTHENTICATION_SCHEME_BEARER = "Bearer";
        String AUTHENTICATION_SCHEME_SESSION = "Session";

        if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BASIC)) {
            return ClientAuthenticationType.CLIENT;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_SESSION)) {
            return ClientAuthenticationType.SESSION;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BEARER)) {
            return ClientAuthenticationType.BEARER;
        } else {
            String username = messageInput.getHeader(SCM_PARAMETER_USERNAME);
            String credential = messageInput.getHeader(SCM_PARAMETER_CREDENTIAL);
            String clientId = messageInput.getHeader(SCM_PARAMETER_CLIENT_ID);
            String clientVersion = messageInput.getHeader(SCM_PARAMETER_CLIENT_VERSION);
            String clientSignature = messageInput.getHeader(SCM_PARAMETER_CLIENT_SIGNATURE);
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(credential)) {
                return ClientAuthenticationType.BASIC;
            }
        }
        return ClientAuthenticationType.ANONYMOUS;
    }

    private String extractAuthenticationValue(MessageInput messageInput) {
        String authorizationHeader = null != messageInput ? messageInput.getHeader(SCM_PARAMETER_AUTHORIZATION) : null;
        if (StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        String[] args = authorizationHeader.split(" ");
        if (args.length < 2)
            return null;
        return args[1];
    }

    private JsonNode extractMessagePayload(TerminalServiceAccess serviceAccess, MessageInput input) throws JsonProcessingException {
        if (input.isForCheck()) {
            return null;
        }
        JsonNode payload = null;
        if (null != input.getBody() && input.getBody() instanceof String && StringUtils.isNotEmpty((String) input.getBody())) {
            payload = objectMapper.readTree((String) input.getBody());
        } else if (null != input.getBody() && input.getBody() instanceof JsonNode) {
            payload = (JsonNode) input.getBody();
        }
        if (null == payload) {
            payload = JsonNodeFactory.instance.nullNode();
        }
        List<String> pathVariables = extractPathVariables(serviceAccess.getService().getAlias());
        for (Iterator<String> iterator = pathVariables.iterator(); iterator.hasNext(); ) {
            String pathVariable = iterator.next();
            String pathVariableValue = input.getHeader(pathVariable);
            if (payload instanceof NullNode) {
                payload = JsonNodeFactory.instance.objectNode();
            } else if (payload instanceof ValueNode && !payload.isObject()) {
                final String VALUE_KEY = "value";
                ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                if (payload instanceof BooleanNode) {
                    objectNode.put(VALUE_KEY, payload.booleanValue());
                } else if (payload instanceof LongNode) {
                    objectNode.put(VALUE_KEY, payload.longValue());
                } else if (payload instanceof ShortNode) {
                    objectNode.put(VALUE_KEY, payload.shortValue());
                } else if (payload instanceof DecimalNode) {
                    objectNode.put(VALUE_KEY, payload.decimalValue());
                } else if (payload instanceof BigIntegerNode) {
                    objectNode.put(VALUE_KEY, payload.bigIntegerValue());
                } else if (payload instanceof IntNode) {
                    objectNode.put(VALUE_KEY, payload.intValue());
                } else if (payload instanceof FloatNode) {
                    objectNode.put(VALUE_KEY, payload.floatValue());
                } else if (payload instanceof DoubleNode) {
                    objectNode.put(VALUE_KEY, payload.doubleValue());
                } else if (payload instanceof TextNode) {
                    objectNode.put(VALUE_KEY, payload.textValue());
                } else {
                    throw new InvalidRequestFormatException("message body");
                }
                payload = objectNode;
            }
            ((ObjectNode) payload).put(pathVariable, pathVariableValue);
        }
        return payload;
    }

    private List<String> extractPathVariables(String urlPattern) {
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
