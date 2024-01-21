package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.integration.inbound.AbstractCamelRestInboundChannelGenerator;
import ir.daneshrefah.scm.core.integration.inbound.rest.CamelHttpResponseBuilder;
import ir.daneshrefah.scm.core.utils.CamelUtils;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerDataProviderService;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CORRELATION_ID;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_HTML;
import static ir.daneshrefah.scm.utils.string.HttpConstants.HTTP_HEADER_CONTENT_TYPE_JSON;

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
                                             DecisionManager decisionManager,
                                             CustomerDataProviderService customerService) {
        super(objectMapper, camelContext, authenticationTemplate,
                producerTemplate, transformerService,
                new CamelHttpResponseBuilder(objectMapper), decisionManager, customerService);
        this.urlBuilder = new DefaultRestUrlBuilder();
    }

    @Override
    protected boolean registerEndpoints() {

        List<TerminalServiceAccess> serviceAccesses = getServices();

        DynamicRouteBuilder routeBuilder = new DynamicRouteBuilder();
        for (Iterator<TerminalServiceAccess> iterator = serviceAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceAccess service = iterator.next();
            if (ServiceImplementationType.PARENT.equals(service.getService().getImplementationType())) {
                continue;
            }
            routeBuilder.registerService(service);
        }
        routeBuilder.registerServiceDocumentation(serviceAccesses);
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

        public void registerServiceDocumentation(List<TerminalServiceAccess> serviceAccesses) {
            OpenAPI openAPI = SwaggerGenerator.generateOpenAPI(getChannel(), serviceAccesses, urlBuilder);

            from("netty-http:http://0.0.0.0:" + port + contextPath + "/api-docs/swagger.json")
                    .routeId("swagger_generator_" + getChannel().getCode())
                    .process(exchange -> {
                        exchange.getMessage().setBody(getObjectMapper().writeValueAsString(openAPI));
                        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_JSON);
                    })
                    .end();
            from("netty-http:http://0.0.0.0:" + port + contextPath + "/api-docs/swagger-ui.html")
                    .routeId("swagger_ui_generator_" + getChannel().getCode())
                    .process(exchange -> {
                        exchange.getMessage().setBody("<h1>Swagger UI</h1>");
                        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_HTML);
                    })
                    .end();
        }

        public void registerService(TerminalServiceAccess serviceAccess) {
            RestUrl restUrl = urlBuilder.build(serviceAccess);
            String inboundUrl = "rest:" + restUrl.getHttpMethod() + ":" + restUrl.getUrl();
            from(inboundUrl)
                    .routeId("ROUTE_DRST_" + serviceAccess.getId())
                    .threads(10, 20, "inbound-rest-" +
                            serviceAccess.getService().getCode().toLowerCase())
                    .end()
                    .doTry()
                    .process(exchange -> {
                        Message message = buildMessage(exchange, serviceAccess);
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
