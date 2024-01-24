package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.HttpInboundExecutor;
import ir.daneshrefah.scm.plugin.api.inbound.ResponseBuilder;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.utils.HttpUtils;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import jakarta.servlet.http.HttpServletRequest;

import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_CONTEXT_PATH;
import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_PORT;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-02
 */
public abstract class AbstractRestInboundChannelGenerator extends AbstractInboundChannelGenerator<HttpServletRequest>
        implements HttpInboundExecutor {

    protected String contextPath;
    protected Integer port;

    public AbstractRestInboundChannelGenerator(ObjectMapper objectMapper, AuthenticationClientTemplate authenticationTemplate,
                                               ServiceProducerTemplate producerTemplate,
                                               TransformerService transformerService,
                                               ResponseBuilder<HttpServletRequest> responseBuilder,
                                               DecisionManager decisionManager,
                                               ErrorHandlerService errorHandlerService,
                                               CustomerService customerService) {
        super(objectMapper, authenticationTemplate, producerTemplate, transformerService,
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
    public final TerminalServiceAccess findService(HttpServletRequest request, String serviceCode) {
        String terminalCode = HttpUtils.extractTerminalCode(request);
        return findService(terminalCode, serviceCode);
    }

    @Override
    public final Message executeService(HttpServletRequest request, String serviceCode, JsonNode payload) {
        TerminalServiceAccess service = findService(request, serviceCode);
        Message message = new Message(null);
        message.setPayload(null != payload ? payload : getObjectMapper().nullNode());
        return executeService(message);
    }

    @Override
    public Message executeService(MessageBuildRequest request, ClientAuthenticationRequest authenticationRequest, String serviceCode) {
        TerminalServiceAccess service = findService(request.getTerminalCode(), serviceCode);
        Message message = buildMessage(request, authenticationRequest, service);
        return executeService(message);
    }

    @Override
    public Channel getChannel() {
        return super.getChannel();
    }

    public abstract boolean initialize();

}
