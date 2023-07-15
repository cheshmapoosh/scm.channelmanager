package ir.daneshrefah.scm.connectorapi.component;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

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
        Object body = exchange.getMessage().getBody();
//        exchange.getMessage().setBody("Nab Hello");
        ScmExchange scmExchange = new ScmExchange(exchange.getMessage().getBody(), exchange.getMessage().getHeaders());
        Object newBody = internalProcess(scmExchange);
        if (null != newBody) {
            exchange.getMessage().setBody(newBody);
        }
    }

    public abstract Object internalProcess(ScmExchange exchange) throws Exception;

}
