package ir.daneshrefah.scm.plugin.api.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.message.Header;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_ACCESS_PARAMETER;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_TERMINAL;

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

    private static MessageGenerator INSTANCE = null;
    private final TerminalService terminalService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public static MessageGenerator getInstance() {
        return INSTANCE;
    }

    public Message buildEmptyMessageFromInput() {
        MessageInput input = MessageInputContext.getCurrentContext();
        String inputTerminalCode = input.getHeader(SCM_PARAMETER_TERMINAL);
        TerminalServiceAccess serviceAccess = findServiceAccess(inputTerminalCode, input.getServiceCode());

        Header header = Header.builder()
                .service(null != serviceAccess ? serviceAccess.getService() : null)
                .build();

        Message message = Message.builder()
                .header(header)
                .status(input.isForCheck() ? MessageStatus.SC_SUCCESS : MessageStatus.SC_PROCESSING)
                .payload(null)
                .build();

        return message;
    }

    public Message buildMessageFromInput() throws Exception {
        MessageInput input = MessageInputContext.getCurrentContext();
        String inputTerminalCode = input.getTerminalCode();
        String inputAccessParameter = input.getHeader(SCM_PARAMETER_ACCESS_PARAMETER);
        if (!input.isForCheck() && StringUtils.isEmpty(inputTerminalCode)) {
            throw new MissingRequiredInputException(SCM_PARAMETER_TERMINAL);
        }
        if (!input.isForCheck() && Objects.isNull(input.getTerminal())) {
            throw new MissingRequiredInputException("Terminal");
        }
        if (!input.isForCheck() && !StringUtils.equalsIgnoreCase(input.getTerminal().getCode(), inputTerminalCode)) {
            throw new InvalidInputException(SCM_PARAMETER_TERMINAL);
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

        Header header = Header.builder()
                .service(!input.isForCheck() ? serviceAccess.getService() : null)
                .build();

        Message message = Message.builder()
                .header(header)
                .status(input.isForCheck() ? MessageStatus.SC_SUCCESS : MessageStatus.SC_PROCESSING)
                .payload(extractMessagePayload(serviceAccess, input))
                .build();

        return message;
    }

    public Message cloneMessage(Message source) {
        Header header = Header.builder()
                .service(source.getHeader().getService())
                .build();
        Message result = Message.builder()
                .header(header)
                .status(MessageStatus.SC_PROCESSING)
                .payload(source.getPayload())
                .errors(source.getErrors())
                .build();
        return result;
    }

    public Message generateInternalMessage(Service service, JsonNode payload) {
        Header header = Header.builder()
                .service(service)
//                .level(level)
//                .parentMessageId(parentMessageId)
                .build();
        Message result = Message.builder()
                .header(header)
                .status(MessageStatus.SC_PROCESSING)
                .payload(null != payload ? payload : NullNode.getInstance())
                .build();
        return result;
    }

    private TerminalServiceAccess findServiceAccess(String terminalCode, String serviceCode) {
        if (StringUtils.isEmpty(terminalCode) || StringUtils.isEmpty(serviceCode))
            return null;
        return terminalService.findTerminalServiceAccessByTerminalCodeAndServiceCode(terminalCode, serviceCode).orElse(null);
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
