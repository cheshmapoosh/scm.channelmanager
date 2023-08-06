package ir.daneshrefah.scm.plugin.api.component;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-05
 */
public abstract class AbstractServiceComponentProvider<T> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected T metadata;
    protected ServiceComponentProvider serviceComponentProvider;

    public AbstractServiceComponentProvider(ServiceComponentProvider serviceComponentProvider) {
        this.serviceComponentProvider = serviceComponentProvider;
        try {
            initServerConfigs();
        } catch (Exception e) {
            LOGGER.error("error init server configs.", e);
            throw new RuntimeException(e);
        }
    }

    public abstract void initServerConfigs() throws Exception;

    public void execute(Message message, ServiceComponent serviceComponent) {
        LocalDateTime startTime = LocalDateTime.now();
        boolean isSuccessful = true;
        String errorMessage = null;

        try {
            Object requestBody = transformRequest(message, serviceComponent);
            Object responseBody = executeServiceComponent(message, requestBody, serviceComponent);
            JsonNode response = transformResponse(responseBody, serviceComponent);
            message.setPayload(response);
        } catch (Exception e) {
            isSuccessful = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            message.addServiceComponentCallEvent(startTime, endTime,
                    message.getMessageComponent().getServiceComponent().getCode(),
                    message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode(),
                    isSuccessful, errorMessage);
        }
    }

    protected abstract JsonNode transformResponse(Object responseBody, ServiceComponent serviceComponent);

    protected abstract Object executeServiceComponent(Message message, Object requestBody, ServiceComponent serviceComponent);

    protected abstract Object transformRequest(Message message, ServiceComponent serviceComponent);

    public ServiceComponentProvider getServiceComponentProvider() {
        return serviceComponentProvider;
    }
}
