package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.core.services.service.ServiceServiceImpl;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.InterceptorConfig;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
@RequiredArgsConstructor
//@Component
public class ServiceAutoConfiguration extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAutoConfiguration.class);

    private final ServiceServiceImpl serviceService;
    private final RestExternalServiceExecutor restExternalServiceExecutor;
    private final ExternalServiceExecutor externalServiceExecutor;
    private final JavaServiceExecutor javaServiceExecutor;
    private final CompositionServiceExecutor compositionServiceExecutor;
    private final ProxyServiceExecutor proxyServiceExecutor;
    private final List<MessageInterceptor> messageInterceptors;

    @Override
    public void configure() {
        initServiceExecutorList(this);
        List<Service> services = serviceService.findCallableServiceList();
        LOGGER.info("service list load completed. count: {}", services.size());
        services
                .stream()
                .filter(this::isPublishableService)
                .forEach(service -> serviceExecutor(service).configureServiceExecution(service, this));
    }

    private ServiceExecutor serviceExecutor(Service service) {
        return switch (service.getImplementationType()) {
            case REST_EXTERNAL -> restExternalServiceExecutor;
            case CUSTOM_EXTERNAL -> externalServiceExecutor;
            case JAVA -> javaServiceExecutor;
            case COMPOSITION -> compositionServiceExecutor;
            case PROXY -> proxyServiceExecutor;
            default -> null;
        };
    }

    private List<ServiceExecutor> serviceExecutors() {
        return  List.of(externalServiceExecutor, javaServiceExecutor, compositionServiceExecutor, proxyServiceExecutor);
    }
    private boolean isPublishableService(Service service) {
        return !ServiceImplementationType.PARENT.equals(service.getImplementationType()) &&
                !ServiceStatus.INACTIVE.equals(service.getStatus());
    }


    private void initServiceExecutorList(RouteBuilder routeBuilder) {
        final List<MessageInterceptor> requestInterceptors = getMessageInterceptors(InterceptorConfig.Type.REQUEST);
        final List<MessageInterceptor> responseInterceptors = getMessageInterceptors(InterceptorConfig.Type.RESPONSE);
        serviceExecutors().forEach(serviceExecutor -> serviceExecutor.init(routeBuilder, requestInterceptors, responseInterceptors));
    }

    private List<MessageInterceptor> getMessageInterceptors(InterceptorConfig.Type interceptorType) {
        return messageInterceptors
                .stream()
                .filter(messageInterceptor -> messageInterceptor.interceptorConfig().getType().equals(interceptorType))
                .sorted(Comparator.comparingInt(interceptor -> interceptor.interceptorConfig().getOrder()))
                .toList();
    }

}
