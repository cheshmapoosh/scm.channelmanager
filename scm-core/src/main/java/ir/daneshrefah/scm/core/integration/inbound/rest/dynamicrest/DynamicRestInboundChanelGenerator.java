package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.integration.inbound.AbstractCamelRestInboundChannelGenerator;
import ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger.SwaggerGenerator;
import ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest.swagger.SwaggerUIGenerator;
import ir.daneshrefah.scm.logging.utils.TraceLogUtils;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.tracing.ActiveSpanManager;
import org.apache.camel.tracing.SpanAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

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

    @Autowired
    public TraceLogUtils traceLogUtils;

    public DynamicRestInboundChanelGenerator(ObjectMapper objectMapper, CamelContext camelContext,
                                             ServiceProducerTemplate producerTemplate,
                                             ErrorHandlerService errorHandlerService) {
        super(objectMapper, camelContext, producerTemplate, errorHandlerService);
        this.urlBuilder = new DefaultRestUrlBuilder();
    }

    @Override
    public boolean registerEndpoints() {

        List<TerminalServiceAccess> serviceAccesses = getServices();

        DynamicRouteBuilder routeBuilder = new DynamicRouteBuilder();
        LOGGER.info("**************** start register services for channel ****************");
        for (Iterator<TerminalServiceAccess> iterator = serviceAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceAccess service = iterator.next();
            if (ServiceImplementationType.PARENT.equals(service.getService().getImplementationType())) {
                continue;
            }
            if (!ServiceStatus.ACTIVE.equals(service.getService().getStatus())) {
                continue;
            }
            routeBuilder.registerService(service);
        }
        routeBuilder.registerServiceDocumentation(serviceAccesses);
        try {
            getContext().addRoutes(routeBuilder);
        } catch (Exception e) {
            LOGGER.error("error define routes.", e);
            LOGGER.info("**************** error register services for channel ****************");
            return false;
        }
        LOGGER.info("**************** end register services for channel *****************");
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
            String swaggerUrl = "/api-docs/swagger.json";
            LOGGER.info("swagger url: " + "http://localhost:" + port + contextPath + swaggerUrl);
            from("netty-http:http://0.0.0.0:" + port + contextPath + swaggerUrl)
                    .routeId("swagger_generator_" + getChannel().getCode())
                    .process(CamelCORSManager::configure)
                    .process(exchange -> {
                        OpenAPI openAPI = SwaggerGenerator.getInstance().generateOpenAPI(getChannel(), serviceAccesses, urlBuilder, contextPath, port);
                        exchange.getMessage().setBody(SwaggerGenerator.getInstance().cleanupSwaggerJson(objectMapper.writeValueAsString(openAPI)));
                        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_JSON);
                    })
                    .end();
            String swaggerUIBody = SwaggerUIGenerator.getInstance().generateCamelUIBody(port, contextPath, swaggerUrl);
            LOGGER.info("swagger-ui url: " + "http://localhost:" + port + contextPath + "/api-docs/swagger-ui.html");
            from("netty-http:http://0.0.0.0:" + port + contextPath + "/api-docs/swagger-ui.html")
                    .routeId("swagger_ui_generator_" + getChannel().getCode())
                    .process(exchange -> {
                        exchange.getMessage().setBody(swaggerUIBody);
                        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, HTTP_HEADER_CONTENT_TYPE_HTML);
                    })
                    .end();
        }

        public void registerService(TerminalServiceAccess serviceAccess) {
            RestUrl restUrl = urlBuilder.build(serviceAccess);
            String inboundUrl = "rest:" + restUrl.getHttpMethod() + ":" + restUrl.getUrl();
            LOGGER.info("*** register inbound '{}' for terminal '{}' with url '{}'.", serviceAccess.getService().getCode(),
                    serviceAccess.getTerminal().getCode(), restUrl.getHttpMethod() + ":" + restUrl.getUrl());
            from(inboundUrl)
                    .routeId("ROUTE_INBOUND_" + serviceAccess.getTerminal().getCode() + "_" + serviceAccess.getService().getCode())
                    .threads(10, 20, "inbound-rest-" +
                            serviceAccess.getService().getCode().toLowerCase())
                    .end()
                    .doTry()
                    .process(CamelCORSManager::configure)
                    .process(exchange -> {
                        MessageInput messageInput = extractMessageInput(exchange, serviceAccess);
                        Message message = execute();
                        exchange.getMessage().setBody(message);
                        SpanAdapter span = ActiveSpanManager.getSpan(exchange);
                        traceLogUtils.recordMessageTrace(message,span);
                    })
                    .process(DynamicRestInboundChanelGenerator.this::buildResponse)
                    .end();
        }

    }

}
