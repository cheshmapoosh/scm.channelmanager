package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<ServiceImplementationType, ServiceExecutor> executorMap = new HashMap<>();
    private final List<MessageInterceptor> messageInterceptors;

    @Override
    public void configure() {
        initServiceExecutorList();
        List<Service> services = serviceService.findCallableServiceList();
        LOGGER.info("service list load completed. count: {}", services.size());
        services
                .stream()
                .filter(this::isPublishableService)
                .forEach(service -> {
                    String serviceCode = service.getCode();
                    // For created dynamic proxy service , the route created by $_proxy ... name , but the service code set as same as
                    // target service.
                    if (service.isProxy()) {
                        serviceCode = service.getTargetProxyCode();
                    }
                    String fromUri = "SVI_" + serviceCode;
                    LOGGER.info("start define service '{}' with uri '{}'", service.getId(), fromUri);
                    RouteDefinition routeDefinition = from("direct:" + fromUri).routeId("SERVICE_" + fromUri);
                    ServiceExecutor serviceExecutor = executorMap.get(service.getImplementationType());
                    serviceExecutor.initServiceExecution(service, routeDefinition);
                    routeDefinition.end();
                });
    }

    private boolean isPublishableService(Service service) {
        return !ServiceImplementationType.PARENT.equals(service.getImplementationType()) &&
               !ServiceStatus.INACTIVE.equals(service.getStatus());
    }


    private void initServiceExecutorList() {
        final List<MessageInterceptor> requestInterceptors = getMessageInterceptors(InterceptorConfig.Type.REQUEST);
        final List<MessageInterceptor> responseInterceptors = getMessageInterceptors(InterceptorConfig.Type.RESPONSE);
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

    private List<MessageInterceptor> getMessageInterceptors(InterceptorConfig.Type interceptorType) {
        return messageInterceptors
                .stream()
                .filter(messageInterceptor -> messageInterceptor.interceptorConfig().getType().equals(interceptorType))
                .sorted(Comparator.comparingInt(interceptor -> interceptor.interceptorConfig().getOrder()))
                .toList();
    }

}
