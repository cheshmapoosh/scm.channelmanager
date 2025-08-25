package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.InboundExecutor;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-05
 */
@Deprecated
public abstract class AbstractPureInboundChannelGenerator extends AbstractInboundChannelGenerator implements InboundExecutor {

    protected AbstractPureInboundChannelGenerator(ObjectMapper objectMapper,
                                                  ServiceProducerTemplate producerTemplate,
                                                  ErrorHandlerService errorHandlerService) {
        super(producerTemplate, errorHandlerService, objectMapper);
    }

    @Override
    public Message executeService() {
        return execute();
    }

    @Override
    public Channel getChannel() {
        return super.getChannel();
    }

}
