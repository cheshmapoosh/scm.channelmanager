package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.HttpInboundExecutor;
import ir.daneshrefah.scm.plugin.api.inbound.MessageBuilder;
import ir.daneshrefah.scm.plugin.api.inbound.ResponseBuilder;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.utils.HttpUtils;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
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

    public AbstractRestInboundChannelGenerator(ObjectMapper objectMapper, EventProducer eventProducer,
                                               AuthenticationClientTemplate authenticationTemplate,
                                               ServiceProducerTemplate producerTemplate,
                                               TransformerService transformerService,
                                               MessageBuilder<HttpServletRequest> messageBuilder,
                                               ResponseBuilder<HttpServletRequest> responseBuilder,
                                               DecisionManager decisionManager) {
        super(objectMapper, eventProducer, authenticationTemplate, producerTemplate, transformerService,
                messageBuilder, responseBuilder, decisionManager);
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
    public final TerminalServiceChannelAccess findService(HttpServletRequest request, String serviceCode) {
        String terminalCode = HttpUtils.extractTerminalCode(request);
        return findService(terminalCode, serviceCode);
    }

    @Override
    public final Message executeService(HttpServletRequest request, String serviceCode, JsonNode payload) {
        TerminalServiceChannelAccess service = findService(request, serviceCode);
        Message message = new Message();
        message.setPayload(null != payload ? payload : getObjectMapper().nullNode());
        return executeService(message);
    }

    public abstract boolean initialize();

}
