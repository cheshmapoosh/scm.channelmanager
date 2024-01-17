package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
public abstract class AbstractJavaService {

    private ServiceProducerTemplate serviceProducerTemplate;


    public AbstractJavaService() {
    }

    public AbstractJavaService(ServiceProducerTemplate serviceComponentExecutor) {
        this.serviceProducerTemplate = serviceComponentExecutor;
    }

    public Object execute(Message message, Service service, Object payload) {
        return internalExecute(message, service, payload);
    }

    protected Object internalExecute(Message message, Service service, Object payload) {
        throw new RuntimeException("service " + service.getCode() + " not implemented.");
    }

}
