package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.plugin.api.inbound.ResponseBuilder;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public abstract class AbstractCamelRestInboundChannelGenerator extends AbstractCamelInboundChannelGenerator {

    protected String contextPath;
    protected Integer port;

    protected AbstractCamelRestInboundChannelGenerator(ObjectMapper objectMapper, CamelContext context,
                                                       AuthenticationClientTemplate authenticationTemplate,
                                                       ServiceProducerTemplate producerTemplate,
                                                       TransformerService transformerService,
                                                       ResponseBuilder<Exchange> responseBuilder,
                                                       DecisionManager decisionManager,
                                                       ErrorHandlerService errorHandlerService,
                                                       CustomerService customerService) {
        super(objectMapper, context, authenticationTemplate, producerTemplate, transformerService,
                responseBuilder, decisionManager, errorHandlerService, customerService);
    }

    @Override
    public boolean initConfig() {
        JsonNode metadata = getMetadata();
        if (null == metadata) {
            LOGGER.error("metadata could not be empty.");
            return false;
        }

        port = (null != metadata.get(CHANNEL_METADATA_REST_PORT) && metadata.get(CHANNEL_METADATA_REST_PORT).isInt()) ?
                metadata.get(CHANNEL_METADATA_REST_PORT).asInt() : null;
        contextPath = (null != metadata.get(CHANNEL_METADATA_REST_CONTEXT_PATH) && metadata.get(CHANNEL_METADATA_REST_CONTEXT_PATH).isTextual()) ?
                metadata.get(CHANNEL_METADATA_REST_CONTEXT_PATH).asText() : null;

        if (null == port || null == contextPath) {
            LOGGER.error("metadata is invalid.");
            return false;
        }
        return initialize();
    }

    @Override
    protected MessageBuildRequest extractMessageBuildRequest(Exchange input, TerminalServiceAccess serviceAccess) {
        return MessageBuildRequest.builder()
                .terminalCode(CamelUtils.getTerminalCodeFromExchange(input))
                .channelCode(getChannel().getCode())
                .contentType(CamelUtils.getContentTypeHeaderFromExchange(input))
                .clientCorrelationId(CamelUtils.getClientCorrelationFromExchange(input))
                .clientTimestamp(CamelUtils.getClientTimestampFromExchange(input))
                .receiveTimestamp(Instant.now())
                .accessParameter(CamelUtils.getAccessParameterFromExchange(input))
                .clientAgent(CamelUtils.getClientAgentFromExchange(input))
                .serverHost(CamelUtils.getServerHostFromExchange(input))
                .clientAddress(CamelUtils.getRemoteAddressFromExchange(input))
                .payload(extractMessagePayload(input, serviceAccess))
                .isForCheck(HTTP_METHOD_OPTIONS.equalsIgnoreCase(CamelUtils.getHttpMethodFromExchange(input)))
                .build();
    }

    private JsonNode extractMessagePayload(Exchange exchange, TerminalServiceAccess serviceAccess) {
        String body = exchange.getMessage().getBody(String.class);
        JsonNode payload = null;
        if (StringUtils.isNotEmpty(body)) {
            try {
                payload = getObjectMapper().readTree(body);
            } catch (JsonProcessingException e) {
                LOGGER.error("error extract message body.", e);
            }
        }
        if (null == payload) {
            payload = JsonNodeFactory.instance.nullNode();
        }
        List<String> pathVariables = extractPathVariables(serviceAccess.getService().getAlias());
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

    protected boolean initialize() {
        return true;
    }

}
