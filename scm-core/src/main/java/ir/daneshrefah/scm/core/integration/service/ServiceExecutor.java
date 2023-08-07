package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
public abstract class ServiceExecutor {

    public void executeService(Service service, Message message) {
        executeInternal(service, message);
    }

    public AbstractTransformer getRequestTransformer(Service service) {
        return getTransformer(service.getRequestTransformerType(), service.getRequestTransformerClass());
    }

    public AbstractTransformer getResponseTransformer(Service service) {
        return getTransformer(service.getResponseTransformerType(), service.getResponseTransformerClass());
    }

    private AbstractTransformer getTransformer(TransformerType transformerType, String responseTransformerClass) {
        if (TransformerType.JAVA.equals(transformerType)) {
            String className = responseTransformerClass;
            return ClassLoader.findBeanOrCreateInstanceOfClass(className, AbstractTransformer.class);

        }
        return null;
    }

    protected abstract void executeInternal(Service service, Message message);

}
