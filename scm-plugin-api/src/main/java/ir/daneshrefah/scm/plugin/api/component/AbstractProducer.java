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
//        String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        Object body = null;
        String contentType = ExchangeHelper.getContentType(exchange);
//        if (MimeTypeUtils.APPLICATION_JSON_VALUE.equals(contentType)) {
//            body = exchange.getMessage().getBody(JsonNode.class);
////            body = exchange.getMessage().getBody(JsonNode.class);
//        } else
//            body = exchange.getMessage().getBody();
////        exchange.getMessage().setBody("Nab Hello");
//        Message scmExchange = new Message(body, contentType, exchange.getMessage().getHeaders());
        Message message = exchange.getMessage().getBody(Message.class);
        LocalDateTime startTime = LocalDateTime.now();
        Object newBody = internalProcess(message);
        LocalDateTime endTime = LocalDateTime.now();
        message.addEvent(EventType.SERVICE_COMPONENT_CALL, startTime, endTime,
                message.getMessageComponent().getServiceComponent().getServiceComponentProvider().getCode());
        if (null != newBody) {
            message.getMessageComponent().setPayload(newBody);
//            exchange.getMessage().setBody(newBody);
        }
    }

    public abstract Object internalProcess(Message message) throws Exception;

}
