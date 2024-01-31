package ir.daneshrefah.scm.core.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
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
@Slf4j
@Service
public class ExternalServiceExecutor extends ServiceExecutor implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<String, AbstractExternalServiceProvider> serviceProviderMap = new HashMap<>();

    @Override
    protected JsonNode executeInternal(ir.daneshrefah.scm.common.model.service.Service service, Message message) {
        ExternalService externalService = (ExternalService) service;
        AbstractExternalServiceProvider provider = serviceProviderMap.get(externalService.getServiceProvider().getCode());
        return provider.execute(message, service);
    }

    public void registerExternalServiceProvider(ExternalServiceProvider serviceProviderModel) {
        if (null == serviceProviderModel)
            return;
        if (serviceProviderMap.containsKey(serviceProviderModel.getCode()))
            return;

        AbstractExternalServiceProvider provider = ClassLoader.findBeanOrCreateInstanceOfClass(serviceProviderModel.getProviderClassName(),
                AbstractExternalServiceProvider.class, serviceProviderModel);
        if (null == provider) {
//            LOGGER.warn("error on create instance of '{}' provider with className '{}'", componentName, componentClassName);
            return;
        }
        boolean isConfigured = provider.initServerConfigs(serviceProviderModel);
        if (isConfigured) {
            serviceProviderMap.put(serviceProviderModel.getCode(), provider);
            log.info("serviceProvider '{}' configured successfully.", serviceProviderModel.getCode());
        } else {
            log.error("error on config serviceProvider '{}'", serviceProviderModel.getCode());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
