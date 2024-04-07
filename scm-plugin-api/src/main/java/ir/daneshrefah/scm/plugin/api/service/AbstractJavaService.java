package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-31
 */
public abstract class AbstractJavaService {

    protected final ObjectMapper objectMapper;
    @Getter
    private final ServiceProducerTemplate serviceProducerTemplate;


    public AbstractJavaService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        this.serviceProducerTemplate = producerTemplate;
        this.objectMapper = objectMapper;
    }

    public Object execute(Message message, Service service, Object payload) {
        return internalExecute(message, service, payload);
    }

    protected Object internalExecute(Message message, Service service, Object payload) {
        throw new RuntimeException("service " + service.getCode() + " not implemented.");
    }

}
