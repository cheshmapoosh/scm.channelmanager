package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.*;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.MessageGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_CONTEXT_PATH;
import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_PORT;
import static ir.daneshrefah.scm.utils.constant.Constants.*;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_METHOD_OPTIONS;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-05
 */
@Slf4j
public abstract class AbstractCamelRestInboundChannelGenerator extends AbstractCamelInboundChannelGenerator {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    protected String contextPath;
    protected Integer port;

    protected AbstractCamelRestInboundChannelGenerator(ObjectMapper objectMapper, CamelContext context,
                                                       MessageGenerator messageGenerator,
                                                       ServiceProducerTemplate producerTemplate,
                                                       ErrorHandlerService errorHandlerService) {
        super(objectMapper, context, messageGenerator, producerTemplate, errorHandlerService);
    }

    @Override
    public boolean initConfig() {
        JsonNode metadata = getMetadata();
        if (null == metadata) {
            log.error("metadata could not be empty.");
            return false;
        }

        port = (null != metadata.get(CHANNEL_METADATA_REST_PORT) && metadata.get(CHANNEL_METADATA_REST_PORT).isInt()) ?
                metadata.get(CHANNEL_METADATA_REST_PORT).asInt() : null;
        contextPath = (null != metadata.get(CHANNEL_METADATA_REST_CONTEXT_PATH) && metadata.get(CHANNEL_METADATA_REST_CONTEXT_PATH).isTextual()) ?
                metadata.get(CHANNEL_METADATA_REST_CONTEXT_PATH).asText() : null;

        if (null == port || null == contextPath) {
            log.error("metadata is invalid.");
            return false;
        }
        return initialize();
    }

    protected MessageInput extractMessageInput(Exchange input, TerminalServiceAccess serviceAccess) {
        String body = input.getMessage().getBody(String.class);
        Map<String, Object> headers = input.getMessage().getHeaders().entrySet().stream()
                .filter(entry -> null != entry.getValue() && entry.getValue().getClass().isAssignableFrom(String.class))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String httpMethod = CamelUtils.getHttpMethodFromExchange(input);
        String inputClientTimestamp = (String) headers.get(SCM_PARAMETER_CLIENT_TIMESTAMP);
        String inputClaimCode = (String) headers.get(SCM_PARAMETER_CLAIM_CODE);
        Instant clientTimestamp = StringUtils.isEmpty(inputClientTimestamp) ? null :
                DateUtils.InstantTools.convertToInstant(inputClientTimestamp); //throw exception
        MessageInput result = HttpMessageInput.builder()
                .headers(headers)
                .terminalCode(CamelUtils.getTerminalCodeFromExchange(input))
                .terminal(serviceAccess.getTerminal())
                .channel(getChannel())
                .serviceCode(serviceAccess.getService().getCode())
                .contentType(CamelUtils.getContentTypeHeaderFromExchange(input))
                .clientRemoteAddress(CamelUtils.getRemoteAddressFromExchange(input))
                .clientId((String) headers.get(SCM_PARAMETER_CLIENT_ID))
                .clientCorrelationId((String) headers.get(SCM_PARAMETER_CLIENT_CORRELATION_ID))
                .clientFlowId((String) headers.get(SCM_PARAMETER_CLIENT_FLOW_ID))
                .clientTimestamp(clientTimestamp)
                .accessParameter((String) headers.get(SCM_PARAMETER_ACCESS_PARAMETER))
                .username((String) headers.get(SCM_PARAMETER_USERNAME))
                .authenticationType(AuthenticationUtils.extractAuthenticationType(
                        (String) headers.get(SCM_PARAMETER_AUTHORIZATION),
                        (String) headers.get(SCM_PARAMETER_USERNAME), (String) headers.get(SCM_PARAMETER_CREDENTIAL)))
                .authenticationValue(AuthenticationUtils.extractAuthenticationValue((String) headers.get(SCM_PARAMETER_AUTHORIZATION)))
                .transactionAuthenticationType(StringUtils.isNotEmpty(inputClaimCode) ? ClientAuthenticationType.BASIC : ClientAuthenticationType.ANONYMOUS)
                .transactionAuthenticationValue(inputClaimCode)
                .serverHost(CamelUtils.getServerHostFromExchange(input))
                .body(body)
                .isForCheck(HTTP_METHOD_OPTIONS.equals(httpMethod))
                .clientAgent(CamelUtils.getClientAgentFromExchange(input))
                .httpUrl(CamelUtils.getHttpUrlFromExchange(input))
                .httpMethod(httpMethod)
                .build();
        MessageInputContext.init(result);

        return result;
    }

    protected Exchange buildResponse(Exchange input) {
        Message message = input.getMessage().getBody(Message.class);
        org.apache.camel.Message responseMessage = input.getMessage();

        prepareResponseHeader(message, responseMessage);
        ObjectNode result = objectMapper.createObjectNode();
        result.putPOJO("status", message.getStatus());
        result.putPOJO("errors", message.getErrors());
        result.set("result", message.getPayload());
        responseMessage.setBody(result);

        return input;
    }

    private void prepareResponseHeader(Message message, org.apache.camel.Message responseMessage) {
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        String contentType = messageInput.getContentType();
        if (StringUtils.isEmpty(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        Header header = message.getHeader();
        responseMessage.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatusMapper.toHttpStatus(message.getStatus()));
        responseMessage.setHeader(Exchange.CONTENT_TYPE, contentType);
        responseMessage.setHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, messageInput.getClientCorrelationId());
        responseMessage.setHeader(Constants.SCM_PARAMETER_CORRELATION_ID, messageInput.getCorrelationId());
        responseMessage.setHeader(Constants.SCM_PARAMETER_CLIENT_TIMESTAMP, messageInput.getClientTimestamp());
        responseMessage.setHeader(Constants.SCM_PARAMETER_RECEIVE_TIMESTAMP, messageInput.getReceiveTimestamp());
        Instant responseTime = Instant.now();
        responseMessage.setHeader(Constants.SCM_PARAMETER_RESPONSE_TIMESTAMP, responseTime);
        String duration = null;
        if (null != messageInput.getReceiveTimestamp()) {
            duration = Duration.between(messageInput.getReceiveTimestamp(), responseTime).toMillis() + "(ms)";
        }
        responseMessage.setHeader(Constants.SCM_PARAMETER_RESPONSE_DURATION, duration);
//        exchange.getMessage().setHeader("Access-Control-Allow-Credentials", "true");
//        exchange.getMessage().setHeader("Access-Control-Allow-Headers", "*");
//        exchange.getMessage().setHeader("Access-Control-Allow-Methods", "*");
//        exchange.getMessage().setHeader("Access-Control-Allow-Origin", "*");
//        exchange.getMessage().setHeader("Access-Control-Max-Age", "3600");*/
    }

    protected boolean initialize() {
        return true;
    }

}
