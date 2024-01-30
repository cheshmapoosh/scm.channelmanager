package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.*;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_CONTEXT_PATH;
import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_PORT;
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

    private static Map<Status, Integer> statusMappingMap = new HashMap<>();
    static {
        statusMappingMap.put(Status.SC_PROCESSING, 500);
        statusMappingMap.put(Status.SC_SUCCESS, 200);
        statusMappingMap.put(Status.SC_ACCESS_DENIED, 403);
        statusMappingMap.put(Status.SC_UNAUTHORIZED, 401);
        statusMappingMap.put(Status.SC_NOT_FOUND, 404);
        statusMappingMap.put(Status.SC_ERROR_VALIDATION, 400);
        statusMappingMap.put(Status.SC_ERROR_DATA_INTEGRITY_VIOLATION, 400);
        statusMappingMap.put(Status.SC_ERROR_SYSTEM, 500);
        statusMappingMap.put(Status.SC_ERROR_BUSINESS, 400);
        statusMappingMap.put(Status.SC_ERROR_UNREACHABLE_PROVIDER, 502);
    }

    protected AbstractCamelRestInboundChannelGenerator(ObjectMapper objectMapper, CamelContext context,
                                                       ServiceProducerTemplate producerTemplate,
                                                       ErrorHandlerService errorHandlerService) {
        super(objectMapper, context, producerTemplate, errorHandlerService);
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

    @Override
    public MessageBuildRequest extractMessageBuildRequest(Exchange input, Service service) {
        MessageBuildRequest result = new MessageBuildRequest();
        try {
            result.setTerminalCode(CamelUtils.getTerminalCodeFromExchange(input));
            result.setServiceCode(service.getCode());
            result.setContentType(CamelUtils.getContentTypeHeaderFromExchange(input));
            result.setClientRemoteAddress(CamelUtils.getRemoteAddressFromExchange(input));
            result.setClientCorrelationId(CamelUtils.getClientCorrelationFromExchange(input));
            result.setClientTimestamp(CamelUtils.getClientTimestampFromExchange(input));
            result.setClientAgent(CamelUtils.getClientAgentFromExchange(input));
            result.setUsername(CamelUtils.getUsernameHeaderFromExchange(input));
            result.setAccessParameter(CamelUtils.getAccessParameterFromExchange(input));
            result.setForCheck(HTTP_METHOD_OPTIONS.equalsIgnoreCase(CamelUtils.getHttpMethodFromExchange(input)));
            result.setReceiveTimestamp(Instant.now());
            result.setServerHost(CamelUtils.getServerHostFromExchange(input));
            String authorizationHeader = CamelUtils.getAuthorizationHeaderFromExchange(input);
            result.setAuthenticationType(extractAuthenticationType(authorizationHeader));
            result.setAuthenticationValue(extractAuthenticationValue(authorizationHeader));
            String transactionValue = CamelUtils.getClaimCodeFromExchange(input);
            result.setTransactionAuthenticationType(StringUtils.isEmpty(transactionValue) ?
                    ClientAuthenticationType.ANONYMOUS : ClientAuthenticationType.BASIC);
            result.setTransactionAuthenticationValue(transactionValue);
            result.setPayload(extractMessagePayload(input, service));
        } catch (Exception e) {
            result.setError(e);
        }
        return result;
    }

    private JsonNode extractMessagePayload(Exchange exchange, Service service) throws JsonProcessingException {
        String body = exchange.getMessage().getBody(String.class);
        JsonNode payload = null;
        if (StringUtils.isNotEmpty(body)) {
            payload = objectMapper.readTree(body);
        }
        if (null == payload) {
            payload = JsonNodeFactory.instance.nullNode();
        }
        List<String> pathVariables = extractPathVariables(service.getAlias());
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

    private ClientAuthenticationType extractAuthenticationType(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return ClientAuthenticationType.ANONYMOUS;
        }

        String AUTHENTICATION_SCHEME_BASIC = "Basic";
        String AUTHENTICATION_SCHEME_BEARER = "Bearer";
        String AUTHENTICATION_SCHEME_SESSION = "Session";

        if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BASIC)) {
            return ClientAuthenticationType.BASIC;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_SESSION)) {
            return ClientAuthenticationType.SESSION;
        } else if (StringUtils.startsWithIgnoreCase(authorizationHeader, AUTHENTICATION_SCHEME_BEARER)) {
            return ClientAuthenticationType.BEARER;
        }
        return ClientAuthenticationType.ANONYMOUS;
    }

    private String extractAuthenticationValue(String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            return null;
        }
        String[] args = authorizationHeader.split(" ");
        if (args.length < 2)
            return null;
        return args[1];
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

    @Override
    public Exchange buildResponse(Exchange input, Message message) {
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
        String contentType = message.getHeader().getRequest().getContentType();
        if (StringUtils.isEmpty(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        Header header = message.getHeader();
        responseMessage.setHeader(Exchange.HTTP_RESPONSE_CODE, statusMappingMap.get(message.getStatus()));
        responseMessage.setHeader(Exchange.CONTENT_TYPE, contentType);
        responseMessage.setHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, header.getRequest().getClientCorrelationId());
        responseMessage.setHeader(Constants.SCM_PARAMETER_CORRELATION_ID, header.getCorrelationId());
        responseMessage.setHeader(Constants.SCM_PARAMETER_CLIENT_TIMESTAMP, header.getRequest().getClientTimestamp());
        responseMessage.setHeader(Constants.SCM_PARAMETER_RECEIVE_TIMESTAMP, header.getRequest().getReceiveTimestamp());
        Instant responseTime = Instant.now();
        responseMessage.setHeader(Constants.SCM_PARAMETER_RESPONSE_TIMESTAMP, responseTime);
        String duration = null;
        if (null != header.getRequest().getReceiveTimestamp()) {
            duration = Duration.between(header.getRequest().getReceiveTimestamp(), responseTime).toMillis() + "(ms)";
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
