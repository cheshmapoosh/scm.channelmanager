package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.plugin.api.model.message.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
public abstract class AbstractJavaService {

    ServiceComponentExecutor serviceComponentExecutor;

    public AbstractJavaService(ServiceComponentExecutor serviceComponentExecutor) {
        this.serviceComponentExecutor = serviceComponentExecutor;
    }

    public void execute(Message message) {
        internalExecute(message);
    }

    protected void callServiceComponent(String serviceComponentProviderCode, String serviceComponentCode,
                                        Message message, Object payload) {
        serviceComponentExecutor.executeServiceComponent(serviceComponentProviderCode, serviceComponentCode, message, payload);

    }
    protected abstract void internalExecute(Message message);

}
