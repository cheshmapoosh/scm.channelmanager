package ir.daneshrefah.scm.plugin.api.component;

//import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.model.message.EventType;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.support.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public abstract class AbstractProducer extends DefaultProducer {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractProducer.class);
    public AbstractProducer(AbstractEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public boolean isSingleton() {
        return super.isSingleton();
    }

    @Override
    public final void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage().getBody(Message.class);
        LocalDateTime startTime = LocalDateTime.now();
        boolean isSuccessful = true;
        String errorMessage = null;
        Object newBody = null;
        try {
            newBody = internalProcess(message);
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
        if (null != newBody) {
            message.getMessageComponent().setPayload(newBody);
        }
    }

    public abstract Object internalProcess(Message message) throws Exception;

}
