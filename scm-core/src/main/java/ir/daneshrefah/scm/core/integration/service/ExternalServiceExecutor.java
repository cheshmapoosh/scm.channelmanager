package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.integration.provider.DefaultRestServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ExternalServiceExecutor extends ServiceExecutor implements ApplicationContextAware {

    private static final String HEADER_ORIGINAL_MESSAGE = "ScmOriginalMessage";
    private static final String HEADER_START_TIME = "ScmProviderStartTime";
    private static final String HEADER_END_TIME = "ScmProviderEndTime";
    private static final String HEADER_REQUEST_BODY = "ScmRequestBody";
    private static final String HEADER_RESPONSE_BODY = "ScmResponseBody";

    @Setter
    private ApplicationContext applicationContext;
    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;
    private final ServiceService serviceService;
    private final Map<String, ExternalServiceProviderExecutor> serviceProviderMap = new HashMap<>();

    @Override
    protected void initConfigs(RouteBuilderDelegator routeBuilder) {
        List<AbstractExternalServiceProvider> providers = serviceService.findServiceProviderList();
        for (Iterator<AbstractExternalServiceProvider> iterator = providers.iterator(); iterator.hasNext(); ) {
            AbstractExternalServiceProvider provider = iterator.next();
            registerExternalServiceProvider(provider, routeBuilder);
        }
    }

    @Override
    protected void defineServiceRoute(ir.daneshrefah.scm.common.model.service.Service service, ProcessorDefinition processorDefinition) {
        processorDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            AbstractExternalService externalService = (AbstractExternalService) message.getHeader().getService();
//            ExternalServiceProviderExecutor provider = serviceProviderMap.get(externalService.getServiceProvider().getCode());
//            JsonNode response = provider.execute(message, externalService);
            JsonNode response = executeServiceProvider(message, externalService);
            message.payload(response);
        });
    }

    public final JsonNode executeServiceProvider(Message message, AbstractExternalService service) {
        Exchange exchange = new DefaultExchange(camelContext);
//        ExternalServiceProviderExecutor provider = serviceProviderMap.get(service.getServiceProvider().getCode());
        String targetEndpoint = "direct:ESP_" + service.getServiceProvider().getCode();
        exchange.getMessage().setBody(message);
        exchange = producerTemplate.send(targetEndpoint, exchange);
//        logOutboundEvent(exchange);
        Exception exception = exchange.getException();
        if (null != exception) {
            throw new RuntimeException(exception);
        }
        Message responseMessage = exchange.getMessage().getBody(Message.class);
        return responseMessage.getPayload();
    }

    private void registerExternalServiceProvider(AbstractExternalServiceProvider serviceProviderModel, RouteBuilderDelegator routeBuilder) {
        if (null == serviceProviderModel)
            return;
        if (serviceProviderMap.containsKey(serviceProviderModel.getCode()))
            return;
        AbstractExternalServiceProviderExecutor provider = (AbstractExternalServiceProviderExecutor) extractServiceProviderExecutorInstance(serviceProviderModel);
        if (Objects.isNull(provider)) {
            return;
        }
        configureRouteDefinition(routeBuilder, provider);
//        provider.configureRouteDefinition(routeDefinition, serviceProviderModel);
        serviceProviderMap.put(serviceProviderModel.getCode(), provider);
    }

    private void configureRouteDefinition(RouteBuilderDelegator routeBuilder, ExternalServiceProviderExecutor provider) {
        String fromUri = "ESP_" + provider.getProviderModel().getCode();
        RouteDefinition routeDefinition = routeBuilder.from("direct:" + fromUri).routeId("ROUTE_" + fromUri);
        routeDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            exchange.setProperty(HEADER_ORIGINAL_MESSAGE, message);
            Object requestBody = transformRequest(provider.getRequestTransformers(), message);
            exchange.getMessage().setBody(requestBody);
            exchange.setProperty(HEADER_REQUEST_BODY, requestBody);
            exchange.setProperty(HEADER_START_TIME, Instant.now());
        });
        provider.intiEndpointCallRouteDefinition(routeDefinition);
        routeDefinition.process(exchange -> {
            exchange.setProperty(HEADER_END_TIME, Instant.now());
            String response = exchange.getMessage().getBody(String.class);
            exchange.setProperty(HEADER_RESPONSE_BODY, response);
            JsonNode jsonResponse = null;
            try {
                jsonResponse = objectMapper.readTree(response);
            } catch (JsonProcessingException e) {
                jsonResponse = objectMapper.valueToTree(response);
            }
            if (null != exchange.getException()) {
                return;
            }
            Message message = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
            message.payload(transformResponse(provider.getResponseTransformers(), message, jsonResponse));
            exchange.getMessage().setBody(message);
        });
        routeDefinition.end();
    }

    private ExternalServiceProviderExecutor extractServiceProviderExecutorInstance(AbstractExternalServiceProvider serviceProviderModel) {
        try {
            ExternalServiceProviderExecutor provider = null;
            if (ServiceProviderProtocol.REST.equals(serviceProviderModel.getProtocol())) {
                provider = applicationContext.getBean(DefaultRestServiceProviderExecutor.class);
            } else if (ServiceProviderProtocol.CUSTOM.equals(serviceProviderModel.getProtocol())) {
                provider = ClassLoader.findBeanOrCreateInstanceOfClass(serviceProviderModel.getProviderClassName(),
                        AbstractExternalServiceProviderExecutor.class, serviceProviderModel);
            }
            if (null == provider) {
                log.warn("error on create instance of '{}' provider with className '{}'", serviceProviderModel.getCode(),
                        serviceProviderModel.getProviderClassName());
                return null;
            }
            provider.init(serviceProviderModel);
        } catch (Exception e) {
            log.error("error register external service provider: " + serviceProviderModel.getCode(), e);
        }
        return null;
    }

}
