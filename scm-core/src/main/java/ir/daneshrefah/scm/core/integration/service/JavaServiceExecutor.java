package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@Service
public class JavaServiceExecutor extends ServiceExecutor {

    @Override
    protected void executeInternal(ir.daneshrefah.scm.plugin.api.model.service.Service service, Message message) {
        JavaService javaServiceModel = (JavaService) service;
        AbstractJavaService javaService = ClassLoader.findBeanOrCreateInstanceOfClass(
                javaServiceModel.getJavaImplementationClassName(), AbstractJavaService.class);
        javaService.execute();
        getRequestTransformer(service);
        getResponseTransformer(service);

    }
}
