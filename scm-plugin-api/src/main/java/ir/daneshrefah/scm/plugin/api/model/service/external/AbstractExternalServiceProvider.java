package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class AbstractExternalServiceProvider<T> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected T metadata;
    protected ExternalServiceProvider externalServiceProvider;

    public abstract void initServerConfigs();

    public Object execute(Message message, Service service, Object requestPayload) {
        return executeInternal(message, requestPayload, service);
    }

    protected abstract Object executeInternal(Message message, Object requestBody, Service service);

    public void setExternalServiceProvider(ExternalServiceProvider externalServiceProvider) {
        this.externalServiceProvider = externalServiceProvider;
    }

}
