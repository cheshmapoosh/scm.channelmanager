package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.integration.service.interceptor.*;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.core.service.TransformerService;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
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
    private final DecisionManager decisionManager;
    private final PersonProfileLoader personProfileLoader;
    private final TransformerService transformerService;
    private final ObjectMapper objectMapper;
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

            String fromUri = "SVI_" + service.getCode();

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
                new CustomerEnrichInterceptor(personProfileLoader),
                new ServiceRequestValidationInterceptor(objectMapper),
                new DecisionManagerInterceptor(decisionManager),
                new ServiceRequestTransformerInterceptor(transformerService));
        final List<MessageInterceptor> responseInterceptors = Arrays.asList(
                new ServiceResponseTransformerInterceptor(transformerService));
        executorMap.put(ServiceImplementationType.EXTERNAL, externalServiceExecutor);
        executorMap.put(ServiceImplementationType.JAVA, javaServiceExecutor);
        executorMap.put(ServiceImplementationType.COMPOSITION, compositionServiceExecutor);
        for (Map.Entry<ServiceImplementationType, ServiceExecutor> entry : executorMap.entrySet()) {
            ServiceImplementationType key = entry.getKey();
            ServiceExecutor executor = entry.getValue();
            executor.init(this, requestInterceptors, responseInterceptors);
        }
    }

}
