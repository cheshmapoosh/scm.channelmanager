package ir.daneshrefah.scm.plugin.api.model.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

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
