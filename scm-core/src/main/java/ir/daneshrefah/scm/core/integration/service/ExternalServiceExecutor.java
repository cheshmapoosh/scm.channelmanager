package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProviderExecutor;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    private ApplicationContext applicationContext;
    private final ServiceService serviceService;
    private final Map<String, ExternalServiceProviderExecutor> serviceProviderMap = new HashMap<>();

    @Override
    protected void initConfigs(RouteBuilderDelegator routeBuilder) {
        List<ExternalServiceProvider> providers = serviceService.findServiceProviderList();
        for (Iterator<ExternalServiceProvider> iterator = providers.iterator(); iterator.hasNext(); ) {
            ExternalServiceProvider provider = iterator.next();
            registerExternalServiceProvider(provider, routeBuilder);
        }
    }

    @Override
    protected void defineServiceRoute(ir.daneshrefah.scm.common.model.service.Service service, ProcessorDefinition processorDefinition) {
        processorDefinition.process(exchange -> {
            Message message = exchange.getMessage().getBody(Message.class);
            ExternalService externalService = (ExternalService) message.getHeader().getServiceAccess().getService();
            ExternalServiceProviderExecutor provider = serviceProviderMap.get(externalService.getServiceProvider().getCode());
            JsonNode response = provider.execute(message, externalService);
            message.payload(response);
        });
    }

    private void registerExternalServiceProvider(ExternalServiceProvider serviceProviderModel, RouteBuilderDelegator routeBuilder) {
        if (null == serviceProviderModel)
            return;
        if (serviceProviderMap.containsKey(serviceProviderModel.getCode()))
            return;

        AbstractExternalServiceProviderExecutor provider = null;
        try {
            provider = ClassLoader.findBeanOrCreateInstanceOfClass(serviceProviderModel.getProviderClassName(),
                    AbstractExternalServiceProviderExecutor.class, serviceProviderModel);
            if (null == provider) {
                log.warn("error on create instance of '{}' provider with className '{}'", serviceProviderModel.getCode(),
                        serviceProviderModel.getProviderClassName());
                return;
            }
            String fromUri = "ESP_" + serviceProviderModel.getCode();
            RouteDefinition routeDefinition = routeBuilder.from("direct:" + fromUri).routeId("ROUTE_" + fromUri);
            provider.configureRouteDefinition(routeDefinition, serviceProviderModel);
        } catch (Exception e) {
            log.error("error register external service provider: " + serviceProviderModel.getCode(), e);
        }
        serviceProviderMap.put(serviceProviderModel.getCode(), provider);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
