package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.integration.inbound.AbstractCamelRestInboundChannelGenerator;
import ir.daneshrefah.scm.core.integration.inbound.rest.CamelHttpResponseBuilder;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
@Component
@Scope("prototype")
public class DynamicRestInboundChanelGenerator extends AbstractCamelRestInboundChannelGenerator {

    private final RestUrlBuilder urlBuilder;

    public DynamicRestInboundChanelGenerator(ObjectMapper objectMapper, CamelContext camelContext,
                                             AuthenticationClientTemplate authenticationTemplate,
                                             ServiceProducerTemplate producerTemplate,
                                             TransformerService transformerService,
                                             DecisionManager decisionManager) {
        super(objectMapper, camelContext, authenticationTemplate,
                producerTemplate, transformerService,
                new CamelHttpResponseBuilder(objectMapper), decisionManager);
        this.urlBuilder = new DefaultRestUrlBuilder();
    }

    @Override
    protected boolean registerEndpoints() {

        List<TerminalServiceChannelAccess> services = getServices();

        DynamicRouteBuilder routeBuilder = new DynamicRouteBuilder();
        for (Iterator<TerminalServiceChannelAccess> iterator = services.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess service = iterator.next();
            if (ServiceImplementationType.PARENT.equals(service.getTerminalServiceAccess().getService().getImplementationType())) {
                continue;
            }
            routeBuilder.registerService(service);
        }
        try {
            getContext().addRoutes(routeBuilder);
        } catch (Exception e) {
            LOGGER.error("error define routes.", e);
            return false;
        }
        return true;
    }

    private class DynamicRouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            restConfiguration().host("0.0.0.0").port(port).bindingMode(RestBindingMode.json)
                    .enableCORS(true) // <-- Important
                    .corsAllowCredentials(true) // <-- Important
                    .corsHeaderProperty("Access-Control-Allow-Origin", "*")
                    .corsHeaderProperty("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization")
                    .contextPath(contextPath);
        }

        public void registerService(TerminalServiceChannelAccess service) {
            RestUrl restUrl = urlBuilder.build(service);
            String inboundUrl = "rest:" + restUrl.getHttpMethod() + ":" + restUrl.getUrl();
            from(inboundUrl)
                    .threads(10, 20, "inbound-rest-" +
                            service.getTerminalServiceAccess().getService().getCode().toLowerCase())
                    .end()
                    .doTry()
                    .process(exchange -> {
                        Message message = buildMessage(exchange, service);
                        exchange.getMessage().setBody(message, Message.class);
                    })
                    .process(exchange -> {
                        if (CamelUtils.isInProgress(exchange)) {
                            executeService(exchange.getMessage().getBody(Message.class));
                        }
                    })
                    .process(exchange -> {
                        Message message = exchange.getMessage().getBody(Message.class);
                        exchange = buildResponse(exchange, message);
                    })
                    .doCatch(BaseException.class)
                    .process(exchange -> {
                        BaseException e = exchange.getProperty(ExchangePropertyKey.EXCEPTION_CAUGHT, BaseException.class);
                        exchange.getMessage().setBody(e.getMessage());
                    })
                    .doCatch(Exception.class)
                    .process(exchange -> {
                        ValidationException e = exchange.getProperty(ExchangePropertyKey.EXCEPTION_CAUGHT, ValidationException.class);
                        exchange.getMessage().setBody("errrorrrrr");
                    })
                    .end();
        }

    }

}
