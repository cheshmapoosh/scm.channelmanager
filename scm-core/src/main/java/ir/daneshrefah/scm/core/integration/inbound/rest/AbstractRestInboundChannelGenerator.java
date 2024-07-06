package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.core.integration.inbound.AbstractPureInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import lombok.extern.slf4j.Slf4j;

import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_CONTEXT_PATH;
import static ir.daneshrefah.scm.core.integration.inbound.InboundConstants.CHANNEL_METADATA_REST_PORT;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-02
 */
@Slf4j
public abstract class AbstractRestInboundChannelGenerator extends AbstractPureInboundChannelGenerator {

    protected String contextPath;
    protected Integer port;

    public AbstractRestInboundChannelGenerator(ObjectMapper objectMapper,
                                               ServiceProducerTemplate producerTemplate,
                                               ErrorHandlerService errorHandlerService) {
        super(objectMapper, producerTemplate, errorHandlerService);
    }

    @Override
    public boolean initConfig() {
        JsonNode metadata = getChannel().getMetadata();
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

    public abstract boolean initialize();

}
