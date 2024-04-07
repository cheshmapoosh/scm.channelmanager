package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.core.integration.service.interceptor.*;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.core.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.AbstractInboundChannelGenerator;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
@RequiredArgsConstructor
@Component
public class ServiceAutoConfiguration extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAutoConfiguration.class);

    private final ServiceServiceImpl serviceService;
    private final ExternalServiceExecutor externalServiceExecutor;
    private final JavaServiceExecutor javaServiceExecutor;
    private final CompositionServiceExecutor compositionServiceExecutor;
    private final DecisionManager decisionManager;
    private final CustomerService customerService;
    private final TransformerService transformerService;
    private final ObjectMapper objectMapper;
    private final Map<ServiceImplementationType, ServiceExecutor> executorMap = new HashMap<>();

    @Override
    public void configure() {
        initServiceExecutorList();
        initServiceProviders();
        List<Service> services = serviceService.findCallableServiceList();
        LOGGER.info("service list load completed. count: {}", services.size());
        for (Iterator<Service> iterator = services.iterator(); iterator.hasNext(); ) {
            Service service = iterator.next();
            if (ServiceImplementationType.PARENT.equals(service.getImplementationType())) {
                continue;
            }
            if (ServiceStatus.INACTIVE.equals(service.getStatus())) {
                continue;
            }
            String fromUri = "SVI_" + service.getCode();

            if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType())) {
                externalServiceExecutor.registerExternalServiceProvider(((ExternalService) service).getServiceProvider());
            }

            LOGGER.info("start define service '{}' with uri '{}'", service.getId(), fromUri);
            RouteDefinition routeDefinition = from("direct:" + fromUri).routeId("SERVICE_" + fromUri);
//            routeDefinition.log("service call: " + service.getCode());
//            routeDefinition = service.getImplementation().fullFill(routeDefinition);
            routeDefinition.process(exchange -> {
                ServiceExecutor serviceExecutor = executorMap.get(service.getImplementationType());
                serviceExecutor.executeService(service, exchange.getMessage().getBody(Message.class));
            });
            routeDefinition.end();
        }
    }

    private void initServiceProviders() {
        List<ExternalServiceProvider> serviceProviders = serviceService.findServiceProviderList();
        for (Iterator<ExternalServiceProvider> iterator = serviceProviders.iterator(); iterator.hasNext(); ) {
            ExternalServiceProvider serviceProvider = iterator.next();

            AbstractExternalServiceProviderExecutor serviceProviderExecutor = null;
            try {
                serviceProviderExecutor = ClassLoader.findBeanOrCreateInstanceOfClass(
                        serviceProvider.getProviderClassName(), AbstractExternalServiceProviderExecutor.class);
            } catch (Exception e) {
                LOGGER.error("error on init serviceProvider '" + serviceProvider.getCode() + "' instance.", e);
                continue;
            }
            if (null == serviceProviderExecutor) {
                LOGGER.error("error on init serviceProvider '" + serviceProvider.getCode() + "' instance.");
                continue;
            }
            String fromUri = "ESP_" + serviceProvider.getCode();
            RouteDefinition routeDefinition = from("direct:" + fromUri).routeId("ROUTE_" + fromUri);
            serviceProviderExecutor.configureRouteDefinition(routeDefinition, serviceProvider);
        }
    }

    private void initServiceExecutorList() {
        final List<MessageInterceptor> requestInterceptors = Arrays.asList(
                new CustomerEnrichInterceptor(customerService),
                new ServiceRequestValidationInterceptor(),
                new DecisionManagerInterceptor(decisionManager),
                new ServiceRequestTransformerInterceptor(transformerService));
        final List<MessageInterceptor> responseInterceptors = Arrays.asList(
                new ServiceResponseTransformerInterceptor(transformerService));
        executorMap.put(ServiceImplementationType.EXTERNAL, externalServiceExecutor);
        executorMap.put(ServiceImplementationType.JAVA, javaServiceExecutor);
        executorMap.put(ServiceImplementationType.COMPOSITION, compositionServiceExecutor);
        externalServiceExecutor.setRequestInterceptors(requestInterceptors);
        externalServiceExecutor.setResponseInterceptors(responseInterceptors);
        javaServiceExecutor.setRequestInterceptors(requestInterceptors);
        javaServiceExecutor.setResponseInterceptors(responseInterceptors);
        compositionServiceExecutor.setRequestInterceptors(requestInterceptors);
        compositionServiceExecutor.setResponseInterceptors(responseInterceptors);
    }

}
