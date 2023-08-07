package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
public abstract class AbstractJavaService {

    private final ServiceProducerTemplate serviceProducerTemplate;

    public AbstractJavaService(ServiceProducerTemplate serviceComponentExecutor) {
        this.serviceProducerTemplate = serviceComponentExecutor;
    }

    public void execute(Message message) {
        internalExecute(message);
    }

    protected void callService(Service service, Message message) {
        serviceProducerTemplate.callService(service, message);

    }
    protected abstract void internalExecute(Message message);

}
