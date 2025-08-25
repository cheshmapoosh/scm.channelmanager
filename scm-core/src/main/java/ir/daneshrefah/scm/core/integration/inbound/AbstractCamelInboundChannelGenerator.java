package ir.daneshrefah.scm.core.integration.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.camel.CamelContext;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-05
 */
@Deprecated
public abstract class AbstractCamelInboundChannelGenerator extends AbstractInboundChannelGenerator {

    @Getter(AccessLevel.PROTECTED)
    private final CamelContext context;

    protected AbstractCamelInboundChannelGenerator(ObjectMapper objectMapper, CamelContext context,
                                                   ServiceProducerTemplate producerTemplate,
                                                   ErrorHandlerService errorHandlerService) {
        super(producerTemplate, errorHandlerService, objectMapper);
        this.context = context;
    }

}
