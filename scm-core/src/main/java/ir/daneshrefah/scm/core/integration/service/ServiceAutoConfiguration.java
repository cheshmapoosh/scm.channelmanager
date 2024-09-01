package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.AuthenticationInterceptor;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.RequestValidationInterceptor;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.TerminalRequestTransformerInterceptor;
import ir.daneshrefah.scm.core.integration.inbound.interceptor.TransactionAuthenticationInterceptor;
import ir.daneshrefah.scm.core.integration.service.interceptor.*;
import ir.daneshrefah.scm.core.service.ProxyServiceManager;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.core.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ProxyService;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
public class ServiceAutoConfiguration extends RouteBuilder implements RouteBuilderDelegator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAutoConfiguration.class);

    private final ServiceServiceImpl serviceService;
    private final ExternalServiceExecutor externalServiceExecutor;
    private final JavaServiceExecutor javaServiceExecutor;
    private final CompositionServiceExecutor compositionServiceExecutor;
    private final ProxyServiceExecutor proxyServiceExecutor;
    private final DecisionManager decisionManager;
    private final PersonProfileLoader personProfileLoader;
    private final TransformerService transformerService;
    private final ObjectMapper objectMapper;
    private final AuthenticationClientTemplate authenticationClientTemplate;
    private final Map<ServiceImplementationType, ServiceExecutor> executorMap = new HashMap<>();

    @Override
    public void configure() {
        initServiceExecutorList();
        List<Service> services = serviceService.findCallableServiceList();
        LOGGER.info("service list load completed. count: {}", services.size());
        for (Iterator<Service> iterator = services.iterator(); iterator.hasNext(); ) {
            Service service = iterator.next();
            if (!isPublishableService(service)) {
                continue;
            }

            String serviceCode = service.getCode();
            // For created dynamic proxy service , the route created by $_proxy ... name , but the service code set as same as
            // target service.
            if (service.isProxy()) {
                serviceCode = service.getTargetProxyCode();
            }
            String fromUri = "SVI_" +serviceCode;
            LOGGER.info("start define service '{}' with uri '{}'", service.getId(), fromUri);
            RouteDefinition routeDefinition = from("direct:" + fromUri).routeId("SERVICE_" + fromUri);
            ServiceExecutor serviceExecutor = executorMap.get(service.getImplementationType());
            serviceExecutor.initServiceExecution(service, routeDefinition);
            routeDefinition.end();
        }
    }

    private boolean isPublishableService(Service service) {
        return !ServiceImplementationType.PARENT.equals(service.getImplementationType()) &&
               !ServiceStatus.INACTIVE.equals(service.getStatus());
    }

    private void initServiceExecutorList() {
        final List<MessageInterceptor> requestInterceptors = Arrays.asList(
                new RequestValidationInterceptor(),
                new AuthenticationInterceptor(authenticationClientTemplate),
                new TransactionAuthenticationInterceptor(authenticationClientTemplate),
                new TerminalRequestTransformerInterceptor(),
                new CustomerEnrichInterceptor(personProfileLoader, objectMapper),
                new ServiceRequestValidationInterceptor(objectMapper),
                new DecisionManagerInterceptor(decisionManager),
                new ServiceRequestTransformerInterceptor(transformerService));
        final List<MessageInterceptor> responseInterceptors = List.of(
                new ServiceResponseTransformerInterceptor(transformerService));
        executorMap.put(ServiceImplementationType.CUSTOM_EXTERNAL, externalServiceExecutor);
        executorMap.put(ServiceImplementationType.REST_EXTERNAL, externalServiceExecutor);
        executorMap.put(ServiceImplementationType.JAVA, javaServiceExecutor);
        executorMap.put(ServiceImplementationType.COMPOSITION, compositionServiceExecutor);
        executorMap.put(ServiceImplementationType.PROXY, proxyServiceExecutor);
        for (Map.Entry<ServiceImplementationType, ServiceExecutor> entry : executorMap.entrySet()) {
            ServiceImplementationType key = entry.getKey();
            ServiceExecutor executor = entry.getValue();
            executor.init(this, requestInterceptors, responseInterceptors);
        }
    }

}
