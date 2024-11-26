package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.ServiceProviderActivationStatusException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.service.TransformerService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.support.DefaultExchange;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    @Setter
    private ApplicationContext applicationContext;
    private final CamelContext camelContext;
    private final ProducerTemplate producerTemplate;
    private final ServiceService serviceService;
    private final TransformerService transformerService;
    private final Map<String, ExternalServiceProviderExecutor> serviceProviderMap = new HashMap<>();

//    @Override
//    protected void initConfigs(RouteBuilder routeBuilder) {
//        super.initConfigs(routeBuilder);
//        List<AbstractExternalServiceProvider> providers = serviceService.findServiceProviderList();
//        for (Iterator<AbstractExternalServiceProvider> iterator = providers.iterator(); iterator.hasNext(); ) {
//            AbstractExternalServiceProvider provider = iterator.next();
//            registerExternalServiceProvider(provider, routeBuilder);
//        }
//    }

    @Override
    protected void defineServiceRoute(ir.daneshrefah.scm.common.model.service.Service service, ProcessorDefinition processorDefinition) {
        processorDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            Exchange providerExchange = executeServiceProvider(exchange);
            Message responseMessage = providerExchange.getMessage().getBody(Message.class);
            JsonNode response = responseMessage.getPayload();
            message.payload(response);
        });
    }

    public final Exchange executeServiceProvider(Exchange exchange) {
        Message message = exchange.getMessage().getBody(Message.class);
        AbstractExternalService service = (AbstractExternalService) message.getHeader().getService();

        Exchange providerExchange = new DefaultExchange(camelContext);
        AbstractExternalServiceProvider serviceProvider = service.getServiceProvider();
        if (!serviceProvider.getStatus().equals(ServiceProviderStatus.ACTIVE)){
            throw new ServiceProviderActivationStatusException(serviceProvider.getCode());
        }
        String targetEndpoint = "direct:ESP_" + serviceProvider.getCode();
        providerExchange.getMessage().setBody(message);
        providerExchange = producerTemplate.send(targetEndpoint, providerExchange);
//        logOutboundEvent(exchange);
        Exception providerException = providerExchange.getException();
        if (null != providerException) {
            if (providerException instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            else {
             throw new RuntimeException(providerException);
            }
        }
        return providerExchange;
    }

//    private void registerExternalServiceProvider(AbstractExternalServiceProvider serviceProviderModel, RouteBuilder routeBuilder) {
//        if (null == serviceProviderModel)
//            return;
//        if (serviceProviderMap.containsKey(serviceProviderModel.getCode()))
//            return;
//        AbstractBaseExternalServiceProviderExecutor provider = (AbstractBaseExternalServiceProviderExecutor)
//                extractServiceProviderExecutorInstance(serviceProviderModel);
//        if (Objects.isNull(provider)) {
//            return;
//        }
//        configureRouteDefinition(serviceProviderModel, routeBuilder, provider);
////        provider.configureRouteDefinition(routeDefinition, serviceProviderModel);
//        serviceProviderMap.put(serviceProviderModel.getCode(), provider);
//    }

//    private void configureRouteDefinition(AbstractExternalServiceProvider provider, RouteBuilder routeBuilder,
//                                          ExternalServiceProviderExecutor providerExecutor) {
//        String fromUri = "ESP_" + provider.getCode();
//        RouteDefinition routeDefinition = routeBuilder.from("direct:" + fromUri).routeId("ROUTE_" + fromUri);
//        List<TransformerRelation> transformerRelations = transformerService.findAllTransformerRelationsBySource(
//                provider.getId());
//        routeDefinition.process(exchange -> {
//            Message message = exchange.getMessage().getBody(Message.class);
//            exchange.setProperty(HEADER_ORIGINAL_MESSAGE, message);
//            Object requestBody = transformRequest(
//                    prepareTransformerExecutionWrapper(transformerRelations, TransformerRelationType.SERVICE_PROVIDER_REQUEST), message);
//            exchange.getMessage().setBody(requestBody);
//        });
//        providerExecutor.endpointCallRouteDefinition(routeDefinition);
//        routeDefinition.process(exchange -> {
//            exchange.setProperty(HEADER_END_TIME, Instant.now());
//            String response = exchange.getMessage().getBody(String.class);
//            exchange.setProperty(HEADER_RESPONSE_BODY, response);
//            JsonNode jsonResponse = null;
//            try {
//                jsonResponse = objectMapper.readTree(response);
//            } catch (JsonProcessingException e) {
//                jsonResponse = objectMapper.valueToTree(response);
//            }
//            if (null != exchange.getException()) {
//                return;
//            }
//            Message message = exchange.getProperty(HEADER_ORIGINAL_MESSAGE, Message.class);
//            message.payload(transformResponse(
//                    prepareTransformerExecutionWrapper(transformerRelations, TransformerRelationType.SERVICE_PROVIDER_RESPONSE), message, jsonResponse));
//            exchange.getMessage().setBody(message);
//        });
//        routeDefinition.end();
//    }

//    private ExternalServiceProviderExecutor extractServiceProviderExecutorInstance(AbstractExternalServiceProvider serviceProviderModel) {
//        try {
//            ExternalServiceProviderExecutor provider = null;
//            if (ServiceProviderProtocol.REST.equals(serviceProviderModel.getProtocol())) {
//                provider = applicationContext.getBean(DefaultRestServiceProviderExecutor.class);
//            } else if (ServiceProviderProtocol.CUSTOM.equals(serviceProviderModel.getProtocol())) {
//                provider = ClassLoader.findBeanOrCreateInstanceOfClass(serviceProviderModel.getProviderClassName(),
//                        AbstractBaseExternalServiceProviderExecutor.class, serviceProviderModel);
//            }
//            if (null == provider) {
//                log.warn("error on create instance of '{}' provider with className '{}'", serviceProviderModel.getCode(),
//                        serviceProviderModel.getProviderClassName());
//                return null;
//            }
//            provider.init(serviceProviderModel);
//            return provider;
//        } catch (Exception e) {
//            log.error("error register external service provider: " + serviceProviderModel.getCode(), e);
//        }
//        return null;
//    }

}
