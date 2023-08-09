package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
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
@Service
public class ExternalServiceExecutor extends ServiceExecutor implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<String, AbstractExternalServiceProvider> serviceProviderMap = new HashMap<>();

    @Override
    protected Object executeInternal(ir.daneshrefah.scm.plugin.api.model.service.Service service, Message message, Object requestPayload) {
        ExternalService externalService = (ExternalService) service;
        AbstractExternalServiceProvider provider = serviceProviderMap.get(externalService.getServiceProvider().getCode());
        return provider.execute(message, service, requestPayload);
    }

    public void registerExternalServiceProvider(ExternalServiceProvider serviceProviderModel) {
        if (null == serviceProviderModel)
            return;
        if (serviceProviderMap.containsKey(serviceProviderModel.getCode()))
            return;

        AbstractExternalServiceProvider provider = ClassLoader.findBeanOrCreateInstanceOfClass(serviceProviderModel.getProviderClassName(),
                AbstractExternalServiceProvider.class, serviceProviderModel);
        provider.setExternalServiceProvider(serviceProviderModel);
        if (null == provider) {
//            LOGGER.warn("error on create instance of '{}' provider with className '{}'", componentName, componentClassName);
            return;
        }
        provider.initServerConfigs();
        serviceProviderMap.put(serviceProviderModel.getCode(), provider);
//        LOGGER.info("provider '{}' successfully added to context with '{}' class and '{}' metadata", componentName,
//                componentClassName, componentMetadata);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
