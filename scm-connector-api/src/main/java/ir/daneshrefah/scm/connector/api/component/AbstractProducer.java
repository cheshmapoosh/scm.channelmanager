package ir.daneshrefah.scm.connector.api.component;

//import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.support.ExchangeHelper;
import org.springframework.util.MimeTypeUtils;

public abstract class AbstractProducer extends DefaultProducer {

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
        ScmExchange scmExchange = new ScmExchange(body, contentType, exchange.getMessage().getHeaders());
        Object newBody = internalProcess(scmExchange);
        if (null != newBody) {
            exchange.getMessage().setBody(newBody);
        }
    }

    public abstract Object internalProcess(ScmExchange exchange) throws Exception;

}
